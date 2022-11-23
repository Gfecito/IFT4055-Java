package ift4055;

import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import ift4055.binning.Scheme;
import ift4055.binning.Bin;
import ift4055.binning.elements.Element.*;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class Parser {
    Scheme rootScheme;
    File sam;
    File fasta;
    File GFA;

    public Parser(File sam, File fasta){
        this.sam = sam;
        this.fasta = fasta;
    }

    public Scheme populateBinningScheme() throws IOException {
        rootScheme = new Scheme();

        final Group referenceGenome = storeReferenceGenome();
        final Group pairedReads = storePairedReads();

        return rootScheme;
    }
    public Scheme readAssembly(File GFA) throws IOException, RuntimeException{
        if(rootScheme==null) throw new RuntimeException("Binning scheme needs to be populated first!");
        this.GFA = GFA;

        InputStream I = Files.newInputStream(GFA.toPath());

        Group rootGroup = (Group) rootScheme.getElementByName(fasta.getName());
        rootGroup = rootGroup.readGFA(I);


        return rootScheme;
    }

    private Group storeReferenceGenome() {
        final FastaSequenceFile reader = new FastaSequenceFile(fasta, true);
        Bin root = rootScheme.findBin(0,0);
        LinkedList<Segment> groupChildren = new LinkedList<>();

        // For each chromosome/contig, make a segment.
        ReferenceSequence chromosome = reader.nextSequence();
        System.out.println("Storing reference genome");
        while (chromosome != null) {
            System.out.println("Processing chromosome: "+chromosome.getName()+" of length "+chromosome.length()+" from reference genome");
            int readPointer = 0;
            String name = chromosome.getName();
            String[] split = split(chromosome.toString());

            Segment chainParent = root.newSegment();
            Scheme rootSchemeChild = new Scheme(chainParent, chromosome.length());
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
                int strand = 1; // I guess? Maybe not. TODO
                int rMin = readPointer;
                int wMin = readPointer;
                int offset = 0;

                Bin chainBin = rootSchemeChild.coveringBin(rMin, rMin + length );
                // Add insert to bin in scheme of its segment
                chainBin.newInsert(strand, rMin, span, wMin, bases, offset);

                readPointer += length;
            }

            chromosome = reader.nextSequence();
        }
        // Finished reading.
        reader.close();
        // Put all the children in group.
        Group group = root.newGroup(groupChildren.size());
        int i = 0;
        for (Segment s : groupChildren) {
            group.setChild(i, s); i++;
            s.setParent(group);
        }
        String name = fasta.getName();
        group.setName(name);
        rootScheme.addNamedElement(name, group);
        return group;
    }

    private Group storePairedReads() throws IOException {
        Group u = null;
        final SamReader samReader = SamReaderFactory.makeDefault().open(sam);
        System.out.println("Storing Paired Reads from SAM");

        // From beginning to end.
        SAMRecordIterator iterator = samReader.iterator();
        while (iterator.hasNext()) {
            SAMRecord alignment;

            try{
                alignment = iterator.next(); // "MAPQ should be 0 for unmapped read"... skip this alignment
                if(alignment.getReferenceName().equals("*")){
                    System.out.println("Reached end of named references. Ending paired read binning");
                    break;
                }
                System.out.println("Processing alignment: "+alignment.getReferenceName()+" ; "+alignment.getReadName()+" for paired reads");
            } catch(SAMFormatException e){
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
            Segment x = (Segment) rootScheme.getNamedElement(name); // If scheme not found, instantiate.
            Scheme scheme = x.getScheme();
            if(scheme.equals(rootScheme)) throw new InvalidObjectException("Root scheme should be only for group.");

            int Rmin = alignment.getAlignmentStart() - 1;
            int Rmax = alignment.getAlignmentEnd() - 1;
            if(Rmin>Rmax)
                continue;
            Bin B = scheme.coveringBin(Rmin, Rmax);


            /*
            Lookup the Group element u associated with the template name (QNAME);
            if none (u = null), then initialize u ← B.newGroup(2) with null members and assign u.name.
            */
            name = alignment.getReadName();
            u = (Group) rootScheme.getNamedElement(name);
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
            Segment y = B.newSegment();
            Scheme s = new Scheme(y, alignment.getReadLength());
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
                } else continue;
                offset += span;
            }


            /*
            Retrieve read order from FLAGS, setting i ← 0 for first read, i ← 1 for second (last) read. Then combine:
             */
            int i = ((flags&64)==64)? 1: ((flags&128)==128)? 0: -1;     // -1 for neither first nor second.
            u = combine(u,i,y);
        }
        samReader.close();

        return u;
    }

    private Group combine(Group u, int i, Segment y){
        Segment s = (Segment) u.getChild(i);
        if(s==null){
            u.setChild(y); y.setParent(u);
            Bin lca = Bin.lowestCommonAncestor(u.getBin(),y.getBin());
            if(lca!=u.getBin()) u = y.raiseGroup();
        }
        else{ s = s.combine(y); u = (Group) s.getParent();}

        return u;
    }

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

    private String[] split(String chromosome) {
        return chromosome.split("N");
    }
}