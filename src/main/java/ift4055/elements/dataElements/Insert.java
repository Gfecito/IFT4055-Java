package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;

import java.util.BitSet;

public class Insert extends Element{
    private BitSet strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;

    private Segment parent;
    private int[] indexRange;   // For children. Same bin

    private Insert(BitSet strand, int rMin, int wMin, Segment parent, int[] indexRange){
        this.strand=strand;
        this.rMin=rMin;
        this.wMin=wMin;
        this.parent=parent;
        this.indexRange=indexRange;
    }
    public void setParent(Segment parent) {
        this.parent = parent;
    }

    public class Factory{
        private Insert[] inserts;
        private int length;

        public Insert getElement(int i) {
            return this.inserts[i];
        }

        public void makeElement(BitSet strand, int rMin, int wMin, Segment parent, int[] indexRange){
            this.inserts[this.length] = new Insert(strand, rMin, wMin, parent, indexRange);
            this.length = this.length+1;
        }

        public void deleteElement(int i){
            Insert insert = this.inserts[i];
            insert.setParent(null);
        }
    }
}
