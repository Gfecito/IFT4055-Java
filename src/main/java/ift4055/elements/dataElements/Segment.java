package ift4055.elements.dataElements;
import ift4055.binning.Bin;
import ift4055.interfaces.Element;
import ift4055.interfaces.ranks.Rank2;
import ift4055.interfaces.ranks.Rank3;
import ift4055.interfaces.ranks.Rank4;


public class Segment implements Rank3{
    /**
     * @return The bin that contains this
     */
    @Override
    public Bin getBin() {
        return null;
    }

    /**
     * See Group.java
     */
    @Override
    public boolean isSameBin(Element E) {
        return (this.getBin()==E.getBin());
    }

    private interface SegmentChild extends Rank3, Rank2{}
    private interface SegmentParent extends SegmentChild, Rank4{}

    private SegmentChild child;
    private SegmentParent parent;
    private Rank2[] descendants;

    private Segment(SegmentChild child, SegmentParent parent){
        this.child=child;
        this.parent=parent;
    }

    public void setParent(SegmentParent parent) {
        this.parent = parent;
    }

    public void setChild(SegmentChild child){
        this.child = child;
    }

    public SegmentParent getParent() {
        return parent;
    }

    /**
     * @param E the parent
     */
    @Override
    public void setParent(Element E) {
        this.setParent((SegmentParent) E);
    }

    /**
     * @return the element that contains this
     */
    @Override
    public Element getContainer() {
        return null;
    }

    /**
     * @return root...
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
        setChild((SegmentChild) E);
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
        return 0;
    }

    /**
     *
     */
    @Override
    public void delete() {

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
            toDelete.setParent(null);

            Segment sentinel = this.sentinel;
            Segment head = (Segment) sentinel.getChild();
            sentinel.setChild((SegmentChild) toDelete);
            toDelete.setChild((SegmentChild) head);
        }
    }

}
