package ift4055.elements;

import ift4055.binning.Bin;

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
    }
}
