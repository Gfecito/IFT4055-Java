package ift4055.elements.dataElements;
import ift4055.binning.Bin;
import ift4055.binning.Scheme;
import ift4055.elements.Element;
import ift4055.interfaces.ranks.Rank2;
import ift4055.interfaces.ranks.Rank3;
import ift4055.interfaces.ranks.Rank4;


public class Segment implements Rank3{

    public interface SegmentChild extends Rank3, Rank2{
        public void setParent(Element E);
    }
    public interface SegmentParent extends SegmentChild, Rank4{}

    private String name;
    private SegmentChild child;
    private SegmentParent parent;
    private Rank2[] descendants;
    private Scheme scheme;
    private int index;

    private Segment(SegmentChild child, SegmentParent parent){
        this.child = child;
        this.parent = parent;
    }

    /**
     * @return The bin that contains this
     */
    // In this context, are depth and height equivalent or are they inverses?
    private int[] idx2depth(int j){
        double d,k;
        int alpha = scheme.getAlpha();

        double twoToAlpha = Math.pow(2,alpha);
        d = Math.ceil(Math.log(1+j*(twoToAlpha-1))/alpha);

        double twoToAlphaD = Math.pow(2,alpha*d);
        k =  j - (twoToAlphaD-1)/(twoToAlpha-1);

        int[] depthNOffset = {(int) d, (int) k};
        return depthNOffset;
    }
    @Override
    public Bin getBin() {
        int[] depthNOffset = idx2depth(index);
        int height = depthNOffset[0];
        int offset = depthNOffset[1];
        return scheme.findBin(height, offset);
    }
    public Scheme getScheme(){
        return scheme;
    }

    /**
     * See Group.java
     */
    @Override
    public boolean isSameBin(Element E) {
        return (this.getBin()==E.getBin());
    }

    public SegmentParent getParent() {
        return parent;
    }

    /**
     * @param E the parent
     */
    @Override
    public void setParent(Element E) {
        this.parent = (SegmentParent) E;
    }

    /**
     * @return the element that contains this
     */
    @Override
    public Element getContainer() {
        return parent;
    }

    /**
     * @return root...
     */
    @Override
    public Element getRoot() {
        if(parent==null) return this;
        return this.parent.getRoot();
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Element getChild(int index) { return child; }

    public SegmentChild getChild() { return child; }

    /**
     * @return
     */
    @Override
    public Element[] getMembers() {
        return descendants;
    }

    /**
     * @param index
     * @param E
     */
    @Override
    public void setChild(int index, Element E) {
        setChild(E);
    }

    /**
     * @param E
     */
    @Override
    public void setChild(Element E) {
        this.child = (SegmentChild) E;
    }

    /**
     * @return
     */
    @Override
    public int numChildren() {
        return 1;
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
     * @param index
     * @return
     */
    @Override
    public Base getNucleotideAt(int index) {
        return null;
    }

    /**
     *  Removes element.
     */
    @Override
    public void delete() { this.parent=null; }




    // Update element tree, and return a segment containing x and members of s.
    public static Segment combine(Segment s, SegmentChild x){
        Bin bin = Bin.lowestCommonAncestor(s.getBin(),x.getBin());
        Segment v;
        // Is x a segment, or a match/insert?
        if(x instanceof Rank3) v = (Segment) x;
        else {v=bin.segmentFactory.newSegment();v.setChild(x);x.setParent(v);}

        // Insertion at head
        if(bin==s.getBin()){Element temp=s.getChild();s.setChild(v);v.setParent(temp); return s;}

        // New container in B
        Segment u,w;
        u = bin.segmentFactory.newSegment(); v.setParent(u);
        w = bin.segmentFactory.newSegment();
        w.setChild(s); u.setParent(s.getParent()); u.setChild(w); w.setParent(v);
        Bin uBin, uPBin;
        uBin = u.getBin();
        uPBin = u.getParent().getBin();
        if(Bin.lowestCommonAncestor(uBin,uPBin)!=uPBin) raiseGroup(u);
        return u;
    }
    // Updates binning for rank4 parent of u.
    public static Rank4 raiseGroup(Segment u){
        Element v;
        Group w;
        Bin bin;
        int m;
        v = u.getParent(); bin = Bin.lowestCommonAncestor(u.getBin(),v.getBin());
        if(bin==v.getBin()) return (Rank4) v;

        m = u.getMembers().length;
        w = bin.groupFactory.newGroup(m);
        for (int i = 0; i < m; i++) {
            w.setChild(i,u.getChild(i));
            ((SegmentChild) u.getChild(i)).setParent(w);
        }
        w.setName(u.name);
        u.delete();
        return w;
    }

    public class Factory{
        private int size;
        private Segment sentinel;
        private Segment[] segments;
        public Factory(){
            this.size=32;
            this.segments = new Segment[this.size];
            // Initialize with free segments
            for(int i=1; i<this.size; i++) this.segments[i] = new Segment(null,null);
            // Circle chain through children pointers
            for(int i=1; i<this.size-1; i++) this.segments[i].setChild((SegmentChild) this.segments[i+1]);
            // Close circle (end to start - sentinel)
            this.sentinel = new Segment((SegmentChild) this.segments[1],null);
            this.segments[0] = this.sentinel;
            this.segments[this.size-1].setChild((SegmentChild) this.segments[0]);
        }

        private void increaseCapacity(){
            this.size *= 2;
            Segment[] old = this.segments;
            Segment[] newArray = new Segment[this.size];
            // Copy
            for(int i=0; i<old.length; i++) newArray[i] = old[i];
            // New free segments; circle chained.
            for(int i=old.length; i<this.size; i++) newArray[i] = new Segment(null,null);
            for(int i=old.length; i<this.size-1; i++) newArray[i].setChild((SegmentChild) newArray[i+1]);
            // Close circle chain to sentinel
            newArray[this.size-1].setChild((SegmentChild) this.segments[0]);    // Sentinel
            this.sentinel.setChild((SegmentChild) newArray[old.length]);
            this.segments = newArray;
        }
        public Segment getElement(int index){
            return this.segments[index];
        }
        public void addElement(SegmentChild child, SegmentParent parent){
            makeElement(child, parent);
        }
        public void makeElement(SegmentChild child, SegmentParent parent){
            // The first free instance after the sentinel is deleted from the free chain
            Segment sentinel = this.sentinel;
            Segment walk = (Segment) sentinel.getChild();
            while(walk!=sentinel)
                if(walk.getParent()==null) break;       // Found free segment

            // Free segment found
            if(walk!=sentinel){
                walk.setChild(child);
                walk.setParent(parent);
            }
            // Not found
            else{
                // Expand capacity
                this.increaseCapacity();
                // Search again
                makeElement(child, parent);
            }
        }

        public void deleteElement(int index){
            Segment toDelete = this.segments[index];
            deleteElement(toDelete);
        }
        public void deleteElement(Segment toDelete){
            // Inserted on the free chain at the head, after the sentinel, before the previous head.
            toDelete.delete();

            Segment sentinel = this.sentinel;
            Segment head = (Segment) sentinel.getChild();
            sentinel.setChild((SegmentChild) toDelete);
            toDelete.setChild((SegmentChild) head);
        }


        // Returns a segment instance with child and parent pointing to self.
        public Segment newSegment(){
            Segment segment = new Segment(null,null);
            segment.setChild(segment);
            segment.setParent(segment);
            return segment;
        }
    }

}
