package ift4055.binning.elements;


import ift4055.binning.Bin;
import ift4055.binning.Scheme;
import ift4055.assemblyGraph.Graph.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * For all data elements in our binning scheme
 */
public interface Element {
    /**
     *
     * @return the bin where this element is located
     */
    Bin getBin();

    /**
     * Determines if this element shares a bin with another element.
     * @param E the other element.
     * @return whether it's the same bin.
     */
    default boolean isSameBin(Element E){
        return getBin().equals(E.getBin());
    }

    /**
     *
     * @return the rank of this element in our binning scheme's hierarchy.
     */
    default int getRank(){
        return 1;
    }

    /**
     *
     * @return the parent of this element in our hierarchy.
     */
    default Element getParent(){
        return getParent(0);
    }

    /**
     * By default, elements don't have access to their parents.
     * @param index
     * @return the parent of that index.
     */
    default Element getParent(int index){
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return the 0-th child of this element.
     */
    default Element getChild(){
        return getChild(0);
    }

    /**
     *
     * @param index
     * @return the child at that index.
     */
    default Element getChild(int index){
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the parent of this element.
     * @param E the new parent of this element.
     */
    default void setParent(Element E){
        throw new UnsupportedOperationException();
    }

    // Genome coordinates

    /**
     *
     * @return the starting writing position
     */
    long getWMin();

    /**
     *
     * @return the final writing position
     */
    long getWMax();

    /**
     *
     * @return the lowest reading position
     */
    long getRMin();
    /**
     *
     * @return the highest reading position
     */
    long getRMax();

    /**
     *
     * @return the length of this element.
     */
    default long getLength(){
        return getSpan()+1;
    }

    /**
     *
     * @return the span of this element.
     */
    long getSpan();

    // DNA sequences

    /**
     *
     * @param index
     * @return this element's nucleotide at that index.
     */
    Base getNucleotideAt(int index);

    /**
     * Lowest rank elements.
     */
    interface Rank1 extends Element{}

    /**
     * Represents a single nucleotide, compressed and encoded
     */
    interface Base extends Rank1{
        /**
         * Decodes the base that corresponds to the input syndrome.
         * @param syndrome a syndrome at the same read position as this base.
         * @return the corresponding encoded base.
         */
        Base syndromize(int syndrome);
    }

    /**
     * Represents a mismatch syndrome.
     */
    interface Syndrome extends Rank1{}



    interface Rank2 extends Element{
        @Override
        default int getRank(){
            return 2;
        }

        void setParent(Element E);

        /**
         *
         * @return the segment that contains this element.
         */
        default Element getContainer(){
            return getParent();
        }

        /**
         *
         * @return the root segment of the tree containing this element.
         */
        default Element getRoot(){
            Segment parent = (Segment) getParent();
            return parent.getRoot();
        }

        /**
         *
         * @return the children of this element.
         */
        Element[] getMembers();


        // Strand calculations
        /**
         *
         * @return the forward or backward strand of this element
         */
        int getStrand();

        /**
         *
         * @return whether this element's strand is reversed or forward.
         */
        default boolean isReverseStrand(){
            return ((1-getStrand())/2)==1;
        }

        /**
         * The diagonal, determined by the strand, and both lowest and highest reading positions.
         * @return the transducer state's diagonal.
         */
        default int getDiagonal() {
            long x,y;
            int s;
            s = getStrand();
            x = getRMin();
            y = getRMax();
            return (int) (x-s*(x-y));
        }

        /**
         * Deletes this element (turns to null).
         */
        void delete();

    }

    /**
     * Continuous nucleotide sequences.
     */
    interface Insert extends Rank2{}

    /**
     * Nucleotide sequences that overlap with another segment.
     */
    interface Match extends Rank2{}


    /**
     * A sequence of nucleotides, as well as all its matches.
     */
    interface Segment extends Element{
        @Override
        default int getRank(){
            return 3;
        }

        // Parents and ancestors in the element tree.

        /**
         *
         * @return the scheme defined by this segment.
         */
        Scheme getScheme();

        /**
         *
         * @param s the scheme being set for this segment.
         */
        void setScheme(Scheme s);
        void setParent(Element E);

        /**
         *
         * @return the container of this segment. Can be a group or itself.
         */
        default Element getContainer(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for greater rank (yeah! social mobility!)
            while(up.getRank() == getRank()) up = up.getParent();
            return up;
        }

        /**
         *
         * @return the root of this binning scheme tree.
         */
        default Element getRoot(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for root - no parent =(
            while(up.getParent()!=null) up = up.getParent();
            return up;
        }


        /**
         *
         * @return all the matches and inserts of this segment.
         */
        Element[] getMembers();

        /**
         *
         * @param index the index to set
         * @param E the new child at index.
         */
        void setChild(int index, Element E);

        /**
         *
         * @param E the new child at 0-th index.
         */
        default void setChild(Element E){
            setChild(0,E);
        }

        /**
         *
         * @return the amount of members in this segment.
         */
        default int numChildren(){
            return getMembers().length;
        }

        /**
         *
         * @return the length of this segment.
         */
        default long getLength(){
            return getSpan()+1;
        }

        /**
         *
         * @return the span of this segment.
         */
        long getSpan();

        /**
         * Deletes this segment, adding it to a list of free segments in memory.
         */
        void delete();


        /**
         *
         * @param name the name of this segment.
         */
        void setName(String name);

        /**
         *
         * @return the name of this segment.
         */
        String getName();

        /**
         * Raises the group to which both this and its parent belong,
         * as to make a new group at the lca of this and its parent.
         * @return the new group.
         */
        Group raiseGroup();

        /**
         * Adds an element that has a connection with the present segment.
         * If it overflows the bin in the which the present segment is located: it
         * should make a new segment in a bin covering both the present segment, AND
         * the overflowed element.
         * @param x the element to be added
         * @return this if the element is covered, a new covering segment else.
         */
        Segment combine(Element x);
    }


    interface Group extends Element{
        @Override
        default int getRank(){
            return 4;
        }


        void setParent(Element E);

        /**
         *
         * @return this. A group is its own parent.
         */
        default Element getParent() {
            return this;
        }

        /**
         *
         * @return this. A group is its own container.
         */
        default Element getContainer(){
            return this;
        }

        /**
         *
         * @return this. A group is its own root.
         */
        default Element getRoot(){
            return this;
        }


        /**
         *
         * @return the children of this element.
         */
        Element[] getMembers();

        /**
         *
         * @param index
         * @param E the child to be set at index.
         */
        void setChild(int index, Element E);

        /**
         *
         * @param E the child to be set at index 0.
         */
        default void setChild(Element E){
            setChild(0,E);
        }

        /**
         *
         * @return the amount of children in this group
         */
        default int numChildren(){
            return getMembers().length;
        }


        /**
         * Deletes group.
         */
        void delete();


        /**
         *
         * @param name the name of this group; for a reference group, the .fasta file name works well.
         */
        void setName(String name);

        /**
         *
         * @return the name of this group.
         */
        String getName();



        /*
                            GRAPH
         */

        /**
         * Parses a gfa file into an assembly graph of this group
         * @param I the input stream of the .gfa file we're parsing.
         * @param verbose determines whether the parsing is logged to the console.
         * @return the group with the assembly graph
         * @throws IOException if there's a problem with the .gfa input stream.
         */
        default Group readGFA(InputStream I, boolean verbose, int alpha, int beta) throws IOException {
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(I));
            Group A = this;

            /*
            While reading the GFA segment (S) lines, build a list of Segment elements for the indexed
            identifiers i = 1, 2, . . . , n: if there is already a member A.c[i − 1],
            then use that, or else create a new Segment with its Insert and Base descendants
            initialized from the GFA sequence (in the same binning scheme as A)
             */
            List<Segment> segmentList = new LinkedList<>();
            String line = buffReader.readLine();
            String[] fields = line.split("\t");
            while(line!=null && fields[0].equals("S")){
                if(verbose) System.out.println("Processing S line: "+line);
                int index = Integer.parseInt(fields[1]);
                String sequence = fields[2];
                Segment segment;
                // Make new segment
                if(index>=A.getMembers().length){
                    if(verbose) System.out.println("Creating new segment: "+index+" with sequence: "+sequence);
                    segment = A.getBin().newSegment();
                    segment.setParent(A);
                    int length = sequence.length();
                    Scheme segmentScheme = new Scheme(segment, length, alpha, beta);
                    segment.setScheme(segmentScheme);
                    Bin covering = segmentScheme.coveringBin(0,length);

                    int span = length-1;
                    byte[] dnaSequence = Scheme.toBases(sequence);
                    // Forward strand
                    covering.newInsert(1, 0, span, 0, dnaSequence, 0);
                }
                // Already existant
                else segment = (Segment) A.getChild(index);

                segmentList.add(segment);


                // Create new segment in same binning scheme.


                line = buffReader.readLine();
                fields = line.split("\t");
            }


            /*
            At the end of segment lines, when the first link (L) line is encountered,
            check if A should have more members, i.e., if n is greater than the current number of members in A
            and thus at least one new Segment was created.
            If so, create a new Group A′ for n elements, set child-parent pointers with it, and delete A.
            (If not, set A′ ← A).
             */
            int n;
            if((n=segmentList.size())>A.numChildren()){
                Bin B = A.getBin();
                String name = A.getName();
                A.delete();
                A = B.newGroup(n);
                A.setName(name);
                int i=0;
                for (Segment segment : segmentList) {
                    A.setChild(i,segment); segment.setParent(A);
                    i++;
                }
            }

            //Call initEdge with each Segment child of A′, keep a lookup of the contig edges by child index.
            Edge[] edges = new Edge[A.numChildren()];
            for (int i = 0; i < edges.length; i++) {
                Segment segment = (Segment) A.getChild(i);
                edges[i]= Edge.initEdge(segment);
            }

            /*
            For each link line call C.attachEdges
             */
            while(line!=null && fields[0].equals("L")){
                if(verbose) System.out.println("Processing L line: "+line);
                int fromIndex = Integer.parseInt(fields[1]);
                //String fromOrient = fields[2];
                int toIndex = Integer.parseInt(fields[3]);
                //String toOrient = fields[4];
                //String overlap = fields[5];

                // 1-indexing to 0-indexing:
                fromIndex--; toIndex--;

                Edge from = edges[fromIndex];
                Edge to = edges[toIndex];

                from.attach(0,to,1);



                line = buffReader.readLine();
                if(line==null) break;
                fields = line.split("\t");
            }


            if(verbose) System.out.println("Finished constructing assembly graph!");
            /*
            Return A′.
             */
            return A;
        }
    }
}
