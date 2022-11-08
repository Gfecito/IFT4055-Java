package ift4055.interfaces;

import ift4055.binning.Bin;
import ift4055.elements.dataElements.Base;

public interface Element {
    public default Bin getBin(){
        throw new UnsupportedOperationException();
    }
    public default boolean isSameBin(Element E){
        return getBin().equals(E.getBin());
    }

    public int getRank();

    public default Element getParent(){
        return getParent(0);
    }
    public default Element getParent(int index){
        throw new UnsupportedOperationException();
    }
    public default Element getChild(){
        return getChild(0);
    }
    public default Element getChild(int index){
        throw new UnsupportedOperationException();
    }
    public interface Segment extends Element{

        // Parents and ancestors in the element tree.
        public default void setParent(Element E){
            throw new UnsupportedOperationException();
        }
        public default Element getContainer(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for greater rank (yeah! social mobility!)
            while(up.getRank() == getRank()) up = up.getParent();
            return up;
        }
        public default Element getRoot(){
            Element up = getParent();
            // No parent? Is root.
            if(up==null) return this;
            // Look for root - no parent =(
            while(up.getParent()!=null) up = up.getParent();
            return up;
        }


        // Children and descendants in the element tree
        public default Element[] getMembers(){
            throw new UnsupportedOperationException();
        }
        public default void setChild(int index, Element E){
            throw new UnsupportedOperationException();
        }
        public default void setChild(Element E){
            setChild(0,E);
        }
        public default int numChildren(){
            return getMembers().length;
        }



        // Genome coordinates
        public int getWMin();
        public int getWMax();
        public int getRMin();
        public int getRMax();



        // DNA sequences
        public Base getNucleotideAt(int index);


        // Deletion
        public void delete();
    }

    public interface Rank2 extends Element{
        public void setParent(Element E);
        public default Element getContainer(){
            throw new UnsupportedOperationException();
        }
        public default Element getRoot(){
            throw new UnsupportedOperationException();
        }

        // Children and descendants in the element tree
        public Element[] getMembers();


        // Genome coordinates
        public int getWMin();
        public int getWMax();
        public int getRMin();
        public int getRMax();
        public int getLength();
        public int getSpan();


        // Strand calculations
        public default int getStrand(){
            throw new UnsupportedOperationException();
        }

        public default boolean isReverseStrand(){
            return ((1-getStrand())/2)==1;
        }
        public int getDiagonal();


        // DNA sequences
        public default Element getNucleotideAt(int index){
            return (Rank1) getMembers()[index];
        }


        // Deletion
        public void delete();
    }

    public interface Insert extends Rank2{}
    public interface Match extends Rank2{}


    public interface Rank1 extends Element{
        // DNA sequences
        public default Element getNucleotideAt(int index){
            throw new UnsupportedOperationException();
        }
    }

    public interface Base extends Rank1{}
    public interface Syndrome extends Rank1{}
}
