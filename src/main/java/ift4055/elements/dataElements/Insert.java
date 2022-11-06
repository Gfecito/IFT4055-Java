package ift4055.elements.dataElements;
import ift4055.binning.Bin;
import ift4055.interfaces.Element;
import ift4055.interfaces.ranks.Rank2;

public class Insert implements Rank2 {
    private int strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;
    private int span;

    private Segment parent;
    private int[] indexRange;   // For children. Same bin
    private Base.Factory baseFactory;

    private Insert(int strand, int rMin, int span, int wMin){
        this.strand = strand;
        this.rMin = rMin;
        this.span = span;
        this.wMin = wMin;
        this.baseFactory = new Base.Factory(span+1);
    }
    public void setParent(Segment parent) {
        this.parent = parent;
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
        return getParent();
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

        return parent; // ?
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
        return new Element[0];
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
        return wMin+span;
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
        return 0;
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
        return ((1-strand)/2)==1;
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
        setParent(null);
    }

    public class Factory{
        private Insert[] inserts;
        private int length;
        // dnaSequence might be byte[][]?
        public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
            Insert insert = new Insert(strand, rMin, span, wMin);
            for(int i=0; i<=span; i++) insert.baseFactory.addBase(dnaSequence[i+offset]);
            return insert;
        }

    }
}
