package ift4055.elements.dataElements;
import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.interfaces.Element;
import ift4055.interfaces.ElementMethods;
import ift4055.interfaces.ranks.Rank2;
import ift4055.interfaces.ranks.Rank3;

import java.util.BitSet;

public class Insert implements Rank2 {
    private int strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;

    private Segment parent;
    private int[] indexRange;   // For children. Same bin

    private Insert(int strand, int rMin, int wMin, Segment parent, int[] indexRange){
        this.strand=strand;
        this.rMin=rMin;
        this.wMin=wMin;
        this.parent=parent;
        this.indexRange=indexRange;
    }
    public void setParent(Segment parent) {
        this.parent = parent;
    }

    /**
     * @return
     */
    @Override
    public Bin getBin() {
        return null;
    }

    /**
     * @param E
     * @return
     */
    @Override
    public boolean isSameBin(Element E) {
        return (this.getBin()==E.getBin());
    }

    /**
     * @return
     */
    @Override
    public Element getParent() {
        return parent;
    }

    /**
     * @param E
     */
    @Override
    public void setParent(Element E) {
        this.parent = (Segment) E;
    }

    /**
     * @return
     */
    @Override
    public Element getContainer() {
        return getParent();
    }

    /**
     * @return
     */
    @Override
    public Element getRoot() {
        return null;
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Element getChild(int index) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public Element getChild() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public Element[] getMembers() {
        return new Element[0];
    }

    /**
     * @return
     */
    @Override
    public int getWMin() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getWMax() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getRMin() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getRMax() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getLength() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getSpan() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getStrand() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public boolean isReverseStrand() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public int getDiagonal() {
        return 0;
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Base getNucleotideAt(int index) {
        return null;
    }

    /**
     *
     */
    @Override
    public void delete() {

    }

    public class Factory{
        private Insert[] inserts;
        private int length;

        public Insert getElement(int i) {
            return this.inserts[i];
        }

        public void addElement(int strand, int rMin, int wMin, Segment parent, int[] indexRange){
            makeElement(strand, rMin, wMin, parent, indexRange);
        }
        public void makeElement(int strand, int rMin, int wMin, Segment parent, int[] indexRange){
            this.inserts[this.length] = new Insert(strand, rMin, wMin, parent, indexRange);
            this.length = this.length+1;
        }

        public void deleteElement(int i){
            Insert insert = this.inserts[i];
            deleteElement(insert);
        }
        public void deleteElement(Insert insert){
            insert.setParent(null);
        }
    }
}
