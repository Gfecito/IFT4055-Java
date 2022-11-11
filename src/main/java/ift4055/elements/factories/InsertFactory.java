package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Element.Segment;
import ift4055.elements.Factory;

import java.util.ArrayList;
import java.util.List;

public class InsertFactory implements Factory.Insert {
    private final List<Insert> objects;
    Bin bin;

    public InsertFactory(Bin bin){
        objects = new ArrayList<>();
        this.bin = bin;
    }

    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset) {
        Segment parent = Bin.ref(bin);
        Insert insert = new Insert(strand, rMin, span, wMin);
        // Add children
        objects.add(insert);
        return insert;
    }

    private class Insert implements Element.Insert{
        private final int strand;    // encodes for 00 (-1) or 11 (1)
        private final int rMin;
        private final int wMin;
        private final int span;
        private Segment parent;
        private Base[] indexRange;
        private Insert(int strand, int rMin, int span, int wMin){
            this.strand = strand;
            this.rMin = rMin;
            this.span = span;
            this.wMin = wMin;
            parent = Bin.ref(bin);
        }

        public Bin getBin(){
            throw new UnsupportedOperationException();
        }

        public Element getParent(){
            return getParent(0);
        }
        public Element getParent(int index){
            return parent;
        }
        public Element getChild(){
            return getChild(0);
        }
        public Element getChild(int index){
            return indexRange[index];
        }


        // Genome coordinates

        public int getWMin() {
            return wMin;
        }


        public int getWMax() {
            return wMin + span;
        }


        public int getRMin() {
            return rMin;
        }


        public int getRMax() {
            return rMin + span*strand;
        }




        // DNA sequences
        public Base getNucleotideAt(int index){
            return indexRange[index];
        }



        public void setParent(Element E){
            parent = (Segment) E;
        }

        // Children and descendants in the element tree
        public Element[] getMembers(){
            return indexRange;
        }

        public int getLength(){
            return span+1;
        }
        public int getSpan(){
            return span;
        }


        // Strand calculations
        public int getStrand(){
            return strand;
        }


        // Deletion
        public void delete(){parent=null;}
    }
}
