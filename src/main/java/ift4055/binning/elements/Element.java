package ift4055.binning.elements;


import ift4055.Parser;
import ift4055.binning.Bin;
import ift4055.binning.Scheme;
import ift4055.assemblyGraph.Graph.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public interface Element {
    Bin getBin();
    default boolean isSameBin(Element E){
        return getBin().equals(E.getBin());
    }

    default int getRank(){
        return 1;
    }

    default Element getParent(){
        return getParent(0);
    }
    default Element getParent(int index){
        throw new UnsupportedOperationException();
    }
    default Element getChild(){
        return getChild(0);
    }
    default Element getChild(int index){
        throw new UnsupportedOperationException();
    }

    default void setParent(Element E){
        throw new UnsupportedOperationException();
    }

    // Genome coordinates
    long getWMin();
    long getWMax();
    long getRMin();
    long getRMax();

    default long getLength(){
        return getSpan()+1;
    }
    long getSpan();

    // DNA sequences
    Base getNucleotideAt(int index);

    interface Rank1 extends Element{}

    interface Base extends Rank1{
        Base syndromize(int syndrome);
    }
    interface Syndrome extends Rank1{}



    interface Rank2 extends Element{
        @Override
        default int getRank(){
            return 2;
        }

        void setParent(Element E);
        default Element getContainer(){
            return getParent();
        }
        default Element getRoot(){
            Segment parent = (Segment) getParent();
            return parent.getRoot();
        }

        // Children and descendants in the element tree
        Element[] getMembers();


        // Strand calculations
        int getStrand();

        default boolean isReverseStrand(){
            return ((1-getStrand())/2)==1;
        }
        default int getDiagonal() {
            long x,y;
            int s;
            s = getStrand();
            x = getRMin();
            y = getRMax();
            return (int) (x-s*(x-y));
        }

        // Deletion
        void delete();

    }

    interface Insert extends Rank2{}
    interface Match extends Rank2{}



    interface Segment extends Element{
        @Override
        default int getRank(){
            return 3;
        }

        // Parents and ancestors in the element tree.
        Scheme getScheme();
        void setScheme(Scheme s);
        void setParent(Element E);
        default Element getContainer(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for greater rank (yeah! social mobility!)
            while(up.getRank() == getRank()) up = up.getParent();
            return up;
        }
        default Element getRoot(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for root - no parent =(
            while(up.getParent()!=null) up = up.getParent();
            return up;
        }


        // Children and descendants in the element tree
        Element[] getMembers();
        void setChild(int index, Element E);
        default void setChild(Element E){
            setChild(0,E);
        }
        default int numChildren(){
            return getMembers().length;
        }


        default long getLength(){
            return getSpan()+1;
        }
        long getSpan();

        // Deletion
        void delete();



        // Naming
        void setName(String name);
        String getName();


        Group raiseGroup();
        Segment combine(Element x);
    }


    interface Group extends Element{
        @Override
        default int getRank(){
            return 4;
        }


        // Parents and ancestors in the element tree.
        void setParent(Element E);
        default Element getParent() {
            return this;
        }
        default Element getContainer(){
            return this;
        }
        default Element getRoot(){
            return this;
        }


        // Children and descendants in the element tree
        Element[] getMembers();
        void setChild(int index, Element E);
        default void setChild(Element E){
            setChild(0,E);
        }
        default int numChildren(){
            return getMembers().length;
        }


        // Deletion
        void delete();


        // Naming
        void setName(String name);
        String getName();



        /*
                            GRAPH
         */
        default Group readGFA(InputStream I) throws IOException {
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
                System.out.println("Processing S line: "+line);
                int index = Integer.parseInt(fields[1]);
                String sequence = fields[2];
                Segment segment;
                // Make new segment
                if(index>=A.getMembers().length){
                    System.out.println("Creating new segment: "+index+" with sequence: "+sequence);
                    segment = A.getBin().newSegment();
                    segment.setParent(A);
                    int length = sequence.length();
                    Scheme segmentScheme = new Scheme(segment, length);
                    segment.setScheme(segmentScheme);
                    Bin covering = segmentScheme.coveringBin(0,length);

                    int span = length-1;
                    byte[] dnaSequence = Parser.toBases(sequence);
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
            if((n=segmentList.size())>A.getMembers().length){
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
            Edge[] edges = new Edge[A.getMembers().length];
            for (int i = 0; i < edges.length; i++) {
                Segment segment = (Segment) A.getChild(i);
                edges[i]= Edge.initEdge(segment);
            }

            /*
            For each link line call C.attachEdges
             */
            while(line!=null && fields[0].equals("L")){
                System.out.println("Processing L line: "+line);
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


            System.out.println("Finished constructing assembly graph!");
            /*
            Return A′.
             */
            return A;
        }
    }
}
