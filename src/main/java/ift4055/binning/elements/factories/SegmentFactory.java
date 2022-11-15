package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.Scheme;
import ift4055.binning.elements.Factory;
import ift4055.binning.elements.Element;


public class SegmentFactory implements Factory.Segment {
    private Segment[] objects;
    private Segment sentinel;
    Bin bin;

    public SegmentFactory(Bin b)
    {
        objects = new Segment[16];
        this.bin = b;

        sentinel = new Segment();
        sentinel.parent = sentinel.child = sentinel;
        for (int i = 1; i < objects.length; i++){
            Segment s = new Segment();
            s.parent = null;
            if(i==1) s.child = sentinel;              // End of circular list.
            else s.child = objects[i-1];              // Next in circular list.
            objects[i] = s;
        }
        sentinel.child = objects[objects.length-1];                       // Start of circular list.

        objects[0] = sentinel;
    }

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Segment[] newObjects = new Segment[c];
        for (int i = 0; i < objects.length; i++)
            newObjects[i] = objects[i];
        for (int i = objects.length; i < c; i++){
            Segment s = new Segment();
            s.parent = null;
            if(i==objects.length) s.child = sentinel;     // End of circular list.
            else s.child = newObjects[i-1];               // Next in circular list.
            newObjects[i] = s;
        }
        sentinel.child = newObjects[-1];                        // Start of circular list.

        this.objects = newObjects;
    }

    public Segment newSegment()
    {
        if (sentinel.child == sentinel)
        {
            // if no empty then extend
            expandCapacity();
            Segment s = new Segment();
            s.parent = s.child = s;
            return s;
        } else
        {
            Segment s = (Segment) sentinel.child;
            sentinel.child = s.child;
            s.parent=s.child=s;
            return s;
        }
    }

    /**
     * Multiple Segment children of the same Segment are stored by a circular linked list across the members:
     * Child stores a reference to a rank-3 or rank-2 element,
     * and Parent stores reference to the next sibling,
     * or to the parent Segment if the last child on the list
     */
    private class Segment implements Element.Segment {
        private Element parent;
        private Element child;
        private String name;
        private Scheme scheme;


        public Scheme getScheme(){
            return scheme;
        }

        public Bin getBin() {
            return bin;
        }

        public Element getParent(int index){
            return parent;
        }

        public Element getChild(int index){
            return child;
        }

        public void setParent(Element E) {
            parent = E;
        }

        public Element[] getMembers() {
            if(child.getRank()==2) return new Element[]{child};

            if(child!=null){ Element[] members = {child}; return members;}
            else{ Element[] members = {}; return members;}
        }

        public void setChild(int index, Element E) {
            child = E;
        }

        public long getWMin() {
            return child.getWMin();
        }

        public long getWMax() {
            return child.getWMax();
        }

        public long getRMin() {
            return child.getRMin();
        }

        public long getRMax() {
            return child.getRMax();
        }

        public long getSpan() {
            return child.getSpan();
        }

        public Base getNucleotideAt(int index) {
            // There's an offset here probably TODO
            return child.getNucleotideAt(index);
        }


        public void delete() {
            parent = null;
            child = sentinel.child;
            sentinel.child = this;
        }


        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }




        // Update element tree, and return a segment containing x and members of s.
        public Segment combine(Element x){
            Segment s = this;
            Bin sBin = s.getBin(); Bin xBin = x.getBin();
            Bin bin = Bin.lowestCommonAncestor(s.getBin(),x.getBin());
            Segment v;
            // Is x a segment, or a match/insert?
            if(x.getRank()==3) v = (Segment) x;
            else {v=bin.newSegment();v.setChild(x);x.setParent(v);}

            // Insertion at head
            if(bin==s.getBin()){Element temp=s.getChild();s.setChild(v);v.setParent(temp); return s;}

            // New container in B
            Segment u,w;
            u = bin.newSegment(); v.setParent(u);
            w = bin.newSegment();
            w.setChild(s); u.setParent(s.getParent()); u.setChild(w); w.setParent(v);
            Bin uBin, uPBin;
            uBin = u.getBin();
            uPBin = u.getParent().getBin();
            if(Bin.lowestCommonAncestor(uBin,uPBin)!=uPBin) u.raiseGroup();
            return u;
        }
        // Updates binning for rank4 parent of u.
        public Group raiseGroup(){
            Segment u = this;
            Element v;
            Group w;
            Bin bin;
            int m;
            v = u.getParent(); bin = Bin.lowestCommonAncestor(u.getBin(),v.getBin());
            if(bin==v.getBin()) return (Group) v;

            m = u.getMembers().length;
            w = bin.newGroup(m);
            for (int i = 0; i < m; i++) {
                w.setChild(i,u.getChild(i));
                u.getChild(i).setParent(w);
            }
            w.setName(u.getName());
            u.delete();
            return w;
        }
    }
}
