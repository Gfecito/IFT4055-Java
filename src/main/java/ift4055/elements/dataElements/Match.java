package ift4055.elements.dataElements;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.interfaces.Element;
import ift4055.interfaces.ElementMethods;
import ift4055.interfaces.ranks.Rank2;
import ift4055.interfaces.ranks.Rank3;

import java.util.BitSet;
import java.util.List;

// Placed in the smallest bin covering the interval Rmin(M)..Rmax(M)
public class Match implements Rank2 {
    private int strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;

    private Rank3 parent;
    private int[] indexRange;   // For children. Same bin
    private Base[] children;

    public void setParent(Rank3 parent) {
        this.parent = parent;
    }

    private Match(int strand, int rMin, int wMin, Rank3 parent, Base[] children, int[] indexRange){
        this.strand = strand;
        this.rMin = rMin;
        this.wMin = wMin;
        this.parent = parent;
        this.indexRange = indexRange;
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
        this.parent = (Rank3) E;
    }

    /**
     * @return
     */
    @Override
    public Element getContainer() {
        return null;
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
        return children[index];
    }

    /**
     * @return
     */
    @Override
    public Element getChild() {
        return getChild(0);
    }

    /**
     * @return
     */
    @Override
    public Element[] getMembers() {
        return children;
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
        return (Base) getChild(indexRange[index]);
    }

    /**
     *
     */
    @Override
    public void delete() {
        this.parent = null;
    }

    // M operation of a CIGAR alignment
    public class Factory {
        private Match[] matches;
        private int length;

        public Match getElement(int i) {
            return this.matches[i];
        }

        public void addElement(int strand, int rMin, int wMin, Segment parent, int[] indexRange){
            makeElement(strand, rMin, wMin, parent, indexRange);
        }
        public void makeElement(int strand, int rMin, int wMin, Segment parent, int[] indexRange) {
            this.matches[this.length] = new Match(strand, rMin, wMin, parent, indexRange);
            this.length = this.length + 1;
        }

        public void deleteElement(int i) {
            Match match = this.matches[i];
            match.setParent(null);
        }
    }
}
