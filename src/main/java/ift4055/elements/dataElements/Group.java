package ift4055.elements.dataElements;
import ift4055.binning.Bin;
import ift4055.interfaces.Element;
import ift4055.interfaces.ranks.Rank3;
import ift4055.interfaces.ranks.Rank4;

public class Group implements Rank4 {
    private Rank3[] children;    // fixed-length arrays of rank-3 Group children

    public Group(int childrenCount){
        this.children = new Rank3[childrenCount];
        for (int i = 0; i < childrenCount; i++) this.children[i] = null;
    }


    /**
     * @return the bin that contains this
     */
    @Override
    public Bin getBin() {
        return null;
    }

    /**
     * @param E the element we're checking
     * @return is it the same bin? boolean.
     */
    @Override
    public boolean isSameBin(Element E) {
        return (this.getBin()==E.getBin());
    }

    /**
     * @return Not sure what to do with this.
     */
    @Override
    public Element getParent() {
        return null;
    }

    /**
     * @return Not sure what to do with this.
     */
    @Override
    public Element getContainer() {
        return this;
    }

    /**
     * @return itself, since rank 4.
     */
    @Override
    public Element getRoot() {
        return this;
    }

    /**
     * @param index index of child in array
     * @return the child at index
     */
    @Override
    public Element getChild(int index) {
        return this.children[index];
    }

    /**
     * @return the child at index 0
     */
    @Override
    public Element getChild() {
        return getChild(0);
    }

    /**
     * @return the children array
     */
    @Override
    public Element[] getMembers() {
        return this.children;
    }

    /**
     * @param index its index in the group
     * @param E the child
     */
    @Override
    public void setChild(int index, Element E) {
        this.children[index] = (Segment) E;
    }

    /**
     * @param E the child
     */
    @Override
    public void setChild(Element E) {
        setChild(0,E);
    }

    /**
     * @return the number of instantiated children
     */
    @Override
    public int numChildren() {
        int count = 0;
        for (Rank3 child : this.children)
            if(child!=null) count++;

        return count;
    }

    public void delete(){
        this.children = null;
    }

    public class Factory{
        private int size;
        private Group[] groups;
        public Factory(int size){
            this.size=size;
            this.groups = new Group[this.size];
        }

        public Group getElement(int index){
            return this.groups[index];
        }
        public void addElement(int childrenCount){
            makeElement(childrenCount);
        }
        public void makeElement(int childrenCount){
            this.groups[size] = new Group(childrenCount);
            this.size++;
        }

        public void deleteElement(int i){
            Group group = this.groups[i];
            deleteElement(group);
        }
        public void deleteElement(Group group){
            group.delete();
        }
    }
}
