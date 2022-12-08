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
    private long l;
    private Segment segment;
    private int maximumHeight;
    private Bin[][] bins;
    private HashMap<String, Element>  lookupTable;
    private HashMap<Element, Scheme> childSchemes; // Schemes defined by segments from this scheme
    private String groupName;
    private int alpha = 2;
    private int beta = 10;

    // Root scheme.
    // Everything else will be an indirect child

    /*PARSING
    * CALL ONLY ON ROOT SCHEME
    * */
        public Scheme readAssembly(File GFA) throws IOException, RuntimeException{
            Scheme rootScheme = this;
            InputStream I = Files.newInputStream(GFA.toPath());

            if(groupName==null) throw new RuntimeException("\nNo group was found on the root Scheme! " +
                    "\nBinning scheme needs to be populated first!");
            Element.Group rootGroup = (Element.Group) rootScheme.getElementByName(groupName);
            rootGroup = rootGroup.readGFA(I);


            return rootScheme;
        }

        public Element.Group storeReferenceGenome(File fasta) {
            Scheme rootScheme = this;
            int alpha, beta;        // Coarseness and base resolution respectively
            alpha = 2; beta = 10;

            final FastaSequenceFile reader = new FastaSequenceFile(fasta, true);
            Bin root = this.findBin(0,0);
            LinkedList<Element.Segment> groupChildren = new LinkedList<>();

            // For each chromosome/contig, make a segment.
            ReferenceSequence chromosome = reader.nextSequence();
            System.out.println("Storing reference genome");
            while (chromosome != null) {
                System.out.println("Processing chromosome: "+chromosome.getName()+" of length "+chromosome.length()+" from reference genome");
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
            Element.Group group = root.newGroup(groupChildren.size());
            int i = 0;
            for (Element.Segment s : groupChildren) {
                group.setChild(i, s); i++;
                s.setParent(group);
            }
            groupName = fasta.getName();
            group.setName(groupName);
            rootScheme.addNamedElement(groupName, group);
            return group;
        }

        public Element.Group storePairedReads(File sam) throws IOException {
            Scheme rootScheme = this;
            int alpha, beta;
            alpha = 2; beta = 10;

            Element.Group u = null;
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
                Element.Segment x = (Element.Segment) rootScheme.getNamedElement(name); // If scheme not found, instantiate.
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
                Scheme s =new Scheme(y, alignment.getReadLength(), 2,10);
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
                        y.combine(bin.newInsert(strand, rMin, span, wMin, sequence, offset));
                    } else if (e.getOperator().equals(match)) {
                        bin = scheme.coveringBin(rMin, rMin + offset);
                        y.combine(bin.newMatch(strand, rMin, span, wMin, sequence, offset));
                    } else continue;
                    offset += span;
                }


            /*
            Retrieve read order from FLAGS, setting i ← 0 for first read, i ← 1 for second (last) read. Then combine:
             */
                int i = ((flags&64)==64)? 1: ((flags&128)==128)? 0: -1;     // -1 for neither first nor second.
                //u = combine(u,i,y);
            }
            samReader.close();

            return u;
        }

        private Element.Group combine(Element.Group u, int i, Element.Segment y){
            Element.Segment s = (Element.Segment) u.getChild(i);
            if(s==null){
                u.setChild(y); y.setParent(u);
                Bin lca = Bin.lowestCommonAncestor(u.getBin(),y.getBin());
                if(lca!=u.getBin()) u = y.raiseGroup();
            }
            else{ s = s.combine(y); u = (Element.Group) s.getParent();}

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


    /*DATA STRUCTURE*/
    public Scheme(Segment segment, int length, int alpha, int beta){
        this.alpha = alpha; this.beta = beta;

        this.segment = segment;
        // Root scheme
        if(segment==null){
            bins = new Bin[1][1];
            bins[0][0] = new Bin(0,0,0,this);
            segment = null;
            l = maximumHeight = 0;
            lookupTable = new HashMap<>();
        }
        // Is child of another scheme
        else{
            lookupTable=null;
            l = length;
            int height, width;
            height = maximumHeight = maxHeight();
            height++;
            bins = new Bin[height][];
            for (int i = 0; i < height; i++) {
                int depth = height - i;
                width = maxOffset(i);
                bins[i] = new Bin[width];
            }
        }
    }

    public void addNamedElement(String name, Element e){
        lookupTable.put(name, e);
    }
    public Element getNamedElement(String name){
        return lookupTable.get(name);
    }

    /* PRINCIPAL OPERATIONS */
    // Find bin by height and offset
    public Bin findBin(int index){
        int[] depthAndOffset = idx2depth(index);
        int depth = depthAndOffset[0];
        int offset = depthAndOffset[1];
        return findBin(maximumHeight-depth, offset);
    }
    public Bin findBin(int height, int offset){
        int depth = maximumHeight-height;
        Bin bin = bins[depth][offset];
        if(bin==null) bin = new Bin(depth, height,offset,this);
        bins[depth][offset] = bin;
        return bin;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    // Get max height
    public int maxHeight(){     // Max of scheme
        int start = 0;
        int end = (int) l;
        return coveringHeight(start, end);
    }
    public int coveringHeight(int start, int end){     // Max for section
        int a=alpha;

        long z = (start^end)>>>beta;
        int lg = 64-Long.numberOfLeadingZeros(z);   // bit-length for z in binary representation
        return (lg+a-1)/a;                    // integer division with rounding up
    }
    // Get max offset from height
    public int maxOffset(int height){
        return 1<<(height*alpha+beta);
    }
    // Find and automatically instantiate bin by interval
    public Bin coveringBin(int start, int end){
        int height = coveringHeight(start, end);
        int offset = start >>> (alpha*height+beta);      // bin offset

        return findBin(height,offset);
    }
    // Defining element
    public Segment getSegment(){
        return segment;
    }
    // Indirection depth
    public int getIndirectionDepth(){
        if(segment==null) return 0;
        if(segment.getParent().equals(segment)) return 0;
        if(segment.getParent().getRank()==4) return 1;
        return 1+((Segment) segment.getParent()).getScheme().getIndirectionDepth();
    }
    // Find element
    public Element getElementByName(String name){
        return lookupTable.get(name);
    }
    // Set name
    public void setName(String name, Element element){
        lookupTable.put(name, element);
    }

    public Bin[][] getBins(){
        return bins;
    }
    public long getLength(){
        return l;
    }

    // In this context, height is max at group, min at rank1.
    public int[] idx2depth(int j){
        int d,k;
        int twoToAlpha = 1<<alpha;
        //d = (int) (Math.log(1+j*(twoToAlpha-1))/(float)alpha);
        d = 0;
        while((1<<alpha*d)<j) d++;

        int twoToAlphaD = d>0? twoToAlpha << d : 1;
        k =  j - (twoToAlphaD-1)/(twoToAlpha-1);
        if(k<0) throw new RuntimeException("Negative offset");

        return new int[]{d, k};
    }
    public int depth2idx(int depth, int offset){
        int a = 1<<(alpha*depth);
        int b = 1<<alpha;
        int j = (a-1)/(b-1)+offset;
        if(j<0) throw new RuntimeException("Negative index");
        return j;
    }
}
