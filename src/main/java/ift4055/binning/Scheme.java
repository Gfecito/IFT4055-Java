package ift4055.binning;

import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.Segment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Scheme {
    private final long l;
    private final Segment segment;
    private final int maximumHeight;
    private final Bin[][] bins;
    private final HashMap<String, Element> lookupTable;
    private HashMap<Element, Scheme> childSchemes; // Schemes defined by segments from this scheme
    private String groupName;
    private final int alpha;
    private final int beta;


    /**
     * Parsing, call only on root scheme.
     * @param GFA the file describing the assembly graph.
     * @param verbose determines if we log to console during our parsing.
     * @return the scheme after adding the graph assembly.
     * @throws IOException if there's an error upon reading the supposed .gfa file.
     * @throws RuntimeException if the group for this assembly hasn't been populated.
     */
    public Scheme readAssembly(File GFA, boolean verbose) throws IOException, RuntimeException {
        Scheme rootScheme = this;
        InputStream I = Files.newInputStream(GFA.toPath());

        if (groupName == null) throw new RuntimeException("\nNo group was found on the root Scheme! " +
                "\nBinning scheme needs to be populated first!");
        Element.Group rootGroup = (Element.Group) rootScheme.getNamedElement(groupName);
        rootGroup = rootGroup.readGFA(I, verbose, alpha, beta);


        return rootScheme;
    }

    /**
     * Parsing, call only on root scheme.
     * @param fasta the file describing the reference scheme.
     * @param verbose determines if we log to console during our parsing.
     * @return the scheme after adding the populating with the reference genome.
     */
    public Element.Group storeReferenceGenome(File fasta, boolean verbose) {
        Scheme rootScheme = this;

        final FastaSequenceFile reader = new FastaSequenceFile(fasta, true);
        Bin root = this.findBin(0, 0);
        LinkedList<Element.Segment> groupChildren = new LinkedList<>();

        // For each chromosome/contig, make a segment.
        ReferenceSequence chromosome = reader.nextSequence();
        if (verbose) System.out.println("Storing reference genome");
        while (chromosome != null) {
            if (verbose) System.out.println("Processing chromosome: " + chromosome.getName() + " of length " +
                    chromosome.length() + " from reference genome");
            int readPointer = 0;
            String name = chromosome.getName();
            String[] split = chromosome.getBaseString().split("N");

            Element.Segment chainParent = root.newSegment();
            Scheme rootSchemeChild = new Scheme(chainParent, chromosome.length(), alpha, beta);
            chainParent.setScheme(rootSchemeChild);
            chainParent.setName(name);
            // Each chromosome has a Segment chain.
            groupChildren.add(chainParent);
            rootScheme.addNamedElement(name, chainParent);

            // For each non-ambiguous stretch in the contig, make an insert.
            for (String sequence : split) {
                byte[] bases = toBases(sequence);
                int length = sequence.length();
                int span = length - 1;
                int strand = 1; // Reference strands are forward
                int rMin = readPointer;
                int wMin = readPointer;
                int offset = 0;

                Bin chainBin = rootSchemeChild.coveringBin(rMin, rMin + length);
                // Add insert to bin in scheme of its segment
                chainBin.newInsert(strand, rMin, span, wMin, bases, offset);

                readPointer += length;
            }

            chromosome = reader.nextSequence();
        }
        // Finished reading.
        reader.close();
        // Put all the children in group.
        Element.Group group = root.newGroup(groupChildren.size());
        int i = 0;
        for (Element.Segment s : groupChildren) {
            group.setChild(i, s);
            i++;
            s.setParent(group);
        }
        groupName = fasta.getName();
        group.setName(groupName);
        rootScheme.addNamedElement(groupName, group);
        return group;
    }

    /**
     * Parsing, call only on root scheme.
     * @param sam the file describing the paired reads mapping.
     * @param verbose determines if we log to console during our parsing.
     * @return the scheme after mapping our paired reads.
     * @throws IOException if there's a problem reading the supposed .sam file.
     */
    public Element.Group storePairedReads(File sam, boolean verbose) throws IOException {
        Scheme rootScheme = this;

        Element.Group u = null;
        final SamReader samReader = SamReaderFactory.makeDefault().open(sam);
        if (verbose) System.out.println("Storing Paired Reads from SAM");

        // From beginning to end.
        SAMRecordIterator iterator = samReader.iterator();
        while (iterator.hasNext()) {
            SAMRecord alignment;

            try {
                alignment = iterator.next(); // "MAPQ should be 0 for unmapped read"... skip this alignment
                if (alignment.getReferenceName().equals("*")) {
                    if (verbose) System.out.println("Reached end of named references. Ending paired read binning");
                    break;
                }
                if (verbose) System.out.println("Processing alignment: " + alignment.getReferenceName() + " ; " +
                        alignment.getReadName() + " for paired reads");
            } catch (SAMFormatException e) {
                continue;
            }

            // "All mapped segments in alignment lines are represented on the forward genomic strand"
            // -SAM format specification
            int strand = 1;

            /*
            Lookup the Segment element x associated with the reference contig or chromosome name (RNAME).
            (x should not be null.)
            Retrieve the binning scheme for x (instantiate if it does not exist yet).
            Select the bin B that contains the Rmin..Rmax interval of the alignment.
            Rmin = getAlignmentStart() − 1; Rmax = getAlignmentEnd() − 1.
            */
            String name = alignment.getReferenceName();
            Element.Segment x = (Element.Segment) rootScheme.getNamedElement(name); // If scheme not found, instantiate.
            Scheme scheme = x.getScheme();
            if (scheme.equals(rootScheme)) throw new InvalidObjectException("Root scheme should be only for group.");

            int Rmin = alignment.getAlignmentStart() - 1;
            int Rmax = alignment.getAlignmentEnd() - 1;
            if (Rmin > Rmax)
                continue;
            Bin B = scheme.coveringBin(Rmin, Rmax);


            /*
            Lookup the Group element u associated with the template name (QNAME);
            if none (u = null), then initialize u ← B.newGroup(2) with null members and assign u.name.
            */
            name = alignment.getReadName();
            u = (Element.Group) rootScheme.getNamedElement(name);
            if (u == null) {
                u = B.newGroup(2);
                u.setName(name);
            }



            /*
            Create a container y ← B.newSegment() and add its Match and Insert children z as y ← combine(y, z)
            by parsing the alignment in read position order.
            Match elements are created in the lowest bin that accommodates them;
            Insert elements are placed bin of the previous Match element
            or in B if the alignment starts with insertion.
            Soft-clipping and hard-clipping are not stored.

            Check the strand from FLAGS.
            Then process the CIGAR operations while keeping track of reading and writing positions.
             */
            Element.Segment y = B.newSegment();
            Scheme s = new Scheme(y, alignment.getReadLength(), alpha, beta);
            y.setScheme(s);
            // and add its Match and Insert children z as y ← combine(y, z) by parsing
            // the alignment in read position order
            int flags = alignment.getFlags();
            List<CigarElement> cigarElements = alignment.getCigar().getCigarElements();
            CigarOperator match = CigarOperator.M;
            CigarOperator insert = CigarOperator.I;

            int rMin;
            int span;
            int wMin;
            byte[] sequence = alignment.getReadBases();
            int offset = 0;
            Bin bin = B;

            // Store the rest, only insert and matches for now.
            for (CigarElement e : cigarElements) {
                rMin = wMin = offset;
                span = e.getLength() - 1;

                if (e.getOperator().equals(insert)) {
                    bin.newInsert(strand, rMin, span, wMin, sequence, offset);

                } else if (e.getOperator().equals(match)) {
                    bin = scheme.coveringBin(rMin, rMin + offset);
                    bin.newMatch(strand, rMin, span, wMin, sequence, offset);

                }
                offset += span + 1;
            }


            /*
            Retrieve read order from FLAGS, setting i ← 0 for first read, i ← 1 for second (last) read. Then combine:
             */
            int i = ((flags & 64) == 64) ? 1 : ((flags & 128) == 128) ? 0 : -1;     // -1 for neither first nor second.
            //u = combine(u,i,y);
        }
        samReader.close();

        return u;
    }

    private Element.Group combine(Element.Group u, int i, Element.Segment y) {
        Element.Segment s = (Element.Segment) u.getChild(i);
        if (s == null) {
            u.setChild(y);
            y.setParent(u);
            Bin lca = Bin.lowestCommonAncestor(u.getBin(), y.getBin());
            if (lca != u.getBin()) u = y.raiseGroup();
        } else {
            s = s.combine(y);
            u = (Element.Group) s.getParent();
        }

        return u;
    }

    /**
     *
     * @param sequence textual DNA sequence.
     * @return byte encoded array of same DNA sequence.
     */
    public static byte[] toBases(String sequence) {
        char[] nucleotides = sequence.toUpperCase().toCharArray();
        byte[] bases = new byte[nucleotides.length];
        for (int i = 0; i < nucleotides.length; i++) {
            char nucleotide = nucleotides[i];
            switch (nucleotide) {
                case 'A':
                    bases[i] = 0;
                    break;
                case 'G':
                    bases[i] = 1;
                    break;
                case 'C':
                    bases[i] = 2;
                    break;
                case 'T':
                    bases[i] = 3;
                    break;
            }
        }
        return bases;
    }


    /*DATA STRUCTURE*/
    public Scheme(Segment segment, int length, int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;

        this.segment = segment;
        // Root scheme
        if (segment == null) {
            bins = new Bin[1][1];
            bins[0][0] = new Bin(0, 0, 0, this);
            l = maximumHeight = 0;
            lookupTable = new HashMap<>();
        }
        // Is child of another scheme
        else {
            lookupTable = null;
            l = length;
            int height, width;
            height = maximumHeight = maxHeight();
            height++;
            bins = new Bin[height][];
            for (int i = 0; i < height; i++) {
                width = maxOffset(i);
                bins[i] = new Bin[width];
            }
        }
    }

    /**
     * Saves a named element in this scheme's lookup table.
     * @param name the name of the element.
     * @param e the element.
     */
    public void addNamedElement(String name, Element e) {
        lookupTable.put(name, e);
    }

    /**
     *
     * @param name the name of the element to retrieve.
     * @return the element.
     */
    public Element getNamedElement(String name) {
        return lookupTable.get(name);
    }

    /**
     *
     * @param segment a child segment to be added to the children lookup table, with its scheme.
     */
    public void addSegmentScheme(Segment segment) {
        if(childSchemes==null) childSchemes = new HashMap<>();
        childSchemes.put(segment, segment.getScheme());
    }

    /* PRINCIPAL OPERATIONS */
    /**
     * Finds the bin with the given index.
     * @param index the bin's index.
     * @return the bin.
     */
    public Bin findBin(int index) {
        int[] depthAndOffset = idx2depth(index);
        int depth = depthAndOffset[0];
        int offset = depthAndOffset[1];
        return findBin(maximumHeight - depth, offset);
    }

    /**
     * Returns the bin at the given coordinates; if not yet existent, instantiate it.
     * @param height
     * @param offset
     * @return the bin at that height and that offset.
     */
    public Bin findBin(int height, int offset) {
        int depth = maximumHeight - height;
        Bin bin = bins[depth][offset];
        if (bin == null) bin = new Bin(depth, height, offset, this);
        bins[depth][offset] = bin;
        return bin;
    }

    /**
     *
     * @return the coarseness of this scheme.
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     *
     * @return the base resolution of this scheme.
     */
    public int getBeta() {
        return beta;
    }

    // Get max height

    /**
     *
     * @return the max height of this scheme.
     */
    public int maxHeight() {     // Max of scheme
        int start = 0;
        int end = (int) l;
        return coveringHeight(start, end);
    }

    /**
     *
     * @param start
     * @param end
     * @return the height where you'll find coverage for this interval
     */
    public int coveringHeight(int start, int end) {     // Max for section
        int a = alpha;

        long z = (start ^ end) >>> beta;
        int lg = 64 - Long.numberOfLeadingZeros(z);   // bit-length for z in binary representation
        return (lg + a - 1) / a;                    // integer division with rounding up
    }

    /**
     *
     * @param height
     * @return the maximum offset at that height.
     */
    public int maxOffset(int height) {
        return 1 << (height * alpha + beta);
    }


    /**
     * Find and automatically instantiate bin by interval.
     * @param start
     * @param end
     * @return the bin covering those coordinates.
     */
    public Bin coveringBin(int start, int end) {
        int height = coveringHeight(start, end);
        int offset = start >>> (alpha * height + beta);      // bin offset

        return findBin(height, offset);
    }

    /**
     *
     * @return the defining segment of this scheme.
     */
    public Segment getSegment() {
        return segment;
    }

    /**
     *
     * @return the amount of indirect links from root to this scheme.
     */
    public int getIndirectionDepth() {
        if (segment == null) return 0;
        if (segment.getParent().equals(segment)) return 0;
        if (segment.getParent().getRank() == 4) return 1;
        return 1 + ((Segment) segment.getParent()).getScheme().getIndirectionDepth();
    }

    /**
     *
     * @return the matrix of all bins in this scheme.
     */
    public Bin[][] getBins() {
        return bins;
    }

    /**
     *
     * @return the length of the segment covered by this scheme.
     */
    public long getLength() {
        return l;
    }

    //

    /**
     * Infers depth and offset from a bin's index.
     * In this context, height is max at group, min at rank1.
     * @param j the index
     * @return [depth, offset]
     */
    public int[] idx2depth(int j) {
        int d, k;
        int twoToAlpha = 1 << alpha;
        d = 0;
        while ((1 << alpha * d) < j) d++;

        int twoToAlphaD = d > 0 ? twoToAlpha << d : 1;
        k = j - (twoToAlphaD - 1) / (twoToAlpha - 1);
        if (k < 0) throw new RuntimeException("Negative offset");

        return new int[]{d, k};
    }

    /**
     * Infers index from a bin's depth and offset.
     * @param depth
     * @param offset
     * @return the index.
     */
    public int depth2idx(int depth, int offset) {
        int a = 1 << (alpha * depth);
        int b = 1 << alpha;
        int j = (a - 1) / (b - 1) + offset;
        if (j < 0) throw new RuntimeException("Negative index");
        return j;
    }


    /*
    AFFICHAGE
    */
    @Override
    public String toString() {
        if (groupName != null) {
            String scheme = "This is a root scheme for the group: " + groupName + "\n\n";
            Element.Group group = (Element.Group) getNamedElement(groupName);
            for (Segment segment : (Segment[]) group.getMembers()) scheme += segment.getScheme().toString();
            return scheme;
        }

        String scheme = "Scheme of characteristic segment: " + segment.getName() + " with the following structure:\n\n";
        for (int d = maximumHeight; d >= 0; d--) {
            int width = maxOffset(d);
            for (int k = 0; k < width; k++) {
                Bin bin = bins[d][k];
                if (bin == null) continue;
                scheme += "\nBin at height " + (maximumHeight - d) + " and offset " + k + " was initialized and contains:\n";
                scheme += bin.toString();
            }
        }

        return scheme;
    }
}
