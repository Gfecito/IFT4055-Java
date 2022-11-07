package ift4055.elements.dataElements;

import ift4055.binning.Bin;
import ift4055.interfaces.Element;
import ift4055.interfaces.ranks.Rank2;

// Placed in the smallest bin covering the interval Rmin(M)..Rmax(M)
public class Match implements Rank2 {
    private int strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int span;
    private int wMin;


    private Segment parent;
    private int[] indexRange;   // For children. Same bin
    private Syndrome[] children;

    public void setParent(Segment parent) {
        this.parent = parent;
    }

    private Match(int strand, int rMin, int span, int wMin){
        this.strand = strand;
        this.rMin = rMin;
        this.span = span;
        this.wMin = wMin;
    }

    /**
     * @return
     */
    @Override
    public Bin getBin() {
        return parent.getBin();
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
        return parent.getContainer();
    }

    /**
     * @return
     */
    @Override
    public Element getRoot() {
        return parent.getRoot();
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Element getChild(int index) {
        return (Element) children[index];
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
        return (Element[]) children;
    }

    /**
     * @return
     */
    @Override
    public int getWMin() {
        return wMin;
    }

    /**
     * @return
     */
    @Override
    public int getWMax() {
        return wMin + span;
    }

    /**
     * @return
     */
    @Override
    public int getRMin() {
        return rMin;
    }

    /**
     * @return
     */
    @Override
    public int getRMax() {
        return rMin + span*strand;
    }

    /**
     * @return
     */
    @Override
    public int getLength() {
        return span+1;
    }

    /**
     * @return
     */
    @Override
    public int getSpan() {
        return span;
    }

    /**
     * @return
     */
    @Override
    public int getStrand() {
        return strand;
    }

    /**
     * @return
     */
    @Override
    public boolean isReverseStrand() {
        return ((1-strand)/2)==1;        // Java doesn't allow boolean to int conversion
    }

    /**
     * @return
     */
    @Override
    public int getDiagonal() {
        int x,y,s;
        s = getStrand();
        x = getRMin();
        y = getRMax();
        return x-r*(x-y);
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Base getNucleotideAt(int index) {
        Syndrome syndromeElem = (Syndrome) getChild(indexRange[index]);
        int syndrome = syndromeElem.getSyndrome();
        int readPosition = syndromeElem.getReadPosition();

        return ;
    }

    /**
     * Lazy deletion
     */
    @Override
    public void delete() { this.parent = null; }

    // M operation of a CIGAR alignment
    public class Factory {
        private Match[] matches;
        private int length;
        public Match newMatch(int strand, int rMin, int span, int wMin, byte[][] dnaSequence){
            Match match = new Match(strand, rMin, span, wMin);
            /*
            for(char c: dnaSequence.toCharArray()){
                c = Character.toLowerCase(c);       // Normalize
                int base;
                switch(c){
                    case 'a':
                        base = 0;
                        break;
                    case 'g':
                        base = 1;
                        break;
                    case 'c':
                        base = 2;
                        break;
                    case 't':
                        base = 3;
                        break;
                }
            }*/
            return match;
        }

        public void deleteElement(int i) {
            Match match = this.matches[i];
            match.setParent(null);
        }
    }
}
