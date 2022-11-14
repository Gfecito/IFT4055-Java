package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Element.Segment;
import ift4055.elements.Factory;

public class InsertFactory implements Factory.Insert {
    private Insert[] objects;
    private int index;
    Bin bin;

    public InsertFactory(Bin bin){
        objects = new Insert[16];
        index = 0;
        this.bin = bin;
    }

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Insert[] newObjects = new Insert[c];
        for (int i = 0; i < objects.length; i++)
            newObjects[i] = objects[i];

        this.objects = newObjects;
    }

    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset) {
        Insert insert = new Insert(strand, rMin, span, wMin);
        insert.populateInsert(dnaSequence, offset);

        if(index >= objects.length) expandCapacity();
        objects[index] = insert;
        index++;
        return insert;
    }

    private class Insert implements Element.Insert{
        private final int strand;    // encodes for 00 (-1) or 11 (1)
        private final int rMin;
        private final int wMin;
        private final int span;
        private Segment parent;
        private Base[] children;
        private Insert(int strand, int rMin, int span, int wMin){
            this.strand = strand;
            this.rMin = rMin;
            this.span = span;
            this.wMin = wMin;
            parent = Bin.ref(bin);
            children = new Base[span];
        }

        private void populateInsert(byte[] dnaSequence, int offset){
            for (int i = 0; i < span; i++) {
                children[i] = bin.addBase(dnaSequence[i+offset]);
            }
        }

        public Bin getBin(){
            return bin;
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
            return children[index];
        }


        // Genome coordinates

        public long getWMin() {
            return wMin;
        }


        public long getWMax() {
            return wMin + getLength();
        }


        public long getRMin() {
            return rMin;
        }


        public long getRMax() {
            return rMin + span*getLength();
        }




        // DNA sequences
        public Base getNucleotideAt(int index){
            return children[index];
        }



        public void setParent(Element E){
            parent = (Segment) E;
        }

        // Children and descendants in the element tree
        public Element[] getMembers(){
            return children;
        }

        public long getSpan(){
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
