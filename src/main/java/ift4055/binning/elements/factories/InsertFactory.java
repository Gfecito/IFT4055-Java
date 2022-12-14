package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Factory;

public class InsertFactory implements Factory.Insert {
    private Insert[] objects;
    private int index;
    Bin bin;

    public InsertFactory(Bin bin){
        objects = new Insert[16];
        index = 0;
        this.bin = bin;
    }

    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset) {
        Insert insert = new Insert(strand, rMin, span, wMin);
        insert.populateInsert(dnaSequence, offset);

        if(index >= objects.length) expandCapacity();
        objects[index] = insert;
        index++;
        return insert;
    }

    /**
     * Replace the current object array with a bigger one,
     * to be used whenever the previous one is filled.
     */
    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Insert[] newObjects = new Insert[c];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);

        this.objects = newObjects;
    }

    public Insert[] getInserts(){
        Insert[] inserts = new Insert[index];
        System.arraycopy(objects, 0, inserts, 0, index);
        return inserts;
    }

    private class Insert implements Element.Insert{
        private final int strand;    // encodes for 00 (-1) or 11 (1)
        private final int rMin;
        private final int wMin;
        private final int span;
        private Segment parent;
        private Insert(int strand, int rMin, int span, int wMin){
            this.strand = strand;
            this.rMin = rMin;
            this.span = span;
            this.wMin = wMin;
            parent = Bin.ref(bin);
        }

        /**
         * Adds this insert's nucleotides to the same bin.
         * @param dnaSequence a sequence stream.
         * @param offset where this insert starts in the stream.
         */
        private void populateInsert(byte[] dnaSequence, int offset){
            for (int i = 0; i < span; i++) bin.addBase(dnaSequence[i+offset]);
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
            return getMembers()[index];
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
            return (Base) getMembers()[index];
        }



        public void setParent(Element E){
            parent = (Segment) E;
        }

        // Children and descendants in the element tree
        public Element[] getMembers(){
            Base[] children = new Base[span];
            Base[] binBases = bin.getBases();
            System.arraycopy(binBases, rMin, children, 0, span);

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


        @Override
        public String toString(){
            String insert = "Match of span: "+span;
            insert += (isReverseStrand()?" with reversed strand ":" with forward strand ");
            insert += "starting at read position "+rMin+" and write position "+wMin;
            insert += " with parent "+parent.getName()+"\n";
            return insert;
        }
    }
}
