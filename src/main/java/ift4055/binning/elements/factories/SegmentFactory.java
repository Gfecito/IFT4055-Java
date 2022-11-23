package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.Scheme;
import ift4055.binning.elements.Factory;
import ift4055.binning.elements.Element;

import java.util.LinkedList;
import java.util.List;


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
        System.arraycopy(objects, 0, newObjects, 0, objects.length);
        for (int i = objects.length; i < c; i++){
            Segment s = new Segment();
            s.parent = null;
            if(i==objects.length) s.child = sentinel;     // End of circular list.
            else s.child = newObjects[i-1];               // Next in circular list.
            newObjects[i] = s;
        }
        sentinel.child = newObjects[c-1];                        // Start of circular list.

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
        public void setScheme(Scheme scheme){
            this.scheme = scheme;
        }

        public Bin getBin() {
            return bin;
        }

        public Element getParent(int index){
            return parent;
        }

        public Element getChild(int index){
            if(child.getRank()==2 && child!=this) return child;
            return getMembers()[index];
        }

        public void setParent(Element E) {
            parent = E;
        }

        // Every segment has a scheme for itself, its members are in the scheme.
        public Element[] getMembers() {
            if(child.getRank()==2 && child!=this) return new Element[]{child};
            if(scheme.getBins()==null) return new Element[0];
            Bin[][] bins = scheme.getBins();
            List<Element> members = new LinkedList<>();
            for (Bin[] level:bins) {
                for(Bin bin: level){
                    if(bin==null) continue;
                    Insert[] inserts = bin.getInserts();
                    Match[] matches = bin.getMatches();
                    for (Insert insert : inserts) if(insert!=null) members.add(insert);
                    for (Match match: matches) if(match!=null) members.add(match);
                }
            }
            if(members.isEmpty()) return new Element[0];
            Element[] membersArray = new Element[members.size()];
            membersArray = members.toArray(membersArray);
            return membersArray;
        }

        public void setChild(int index, Element E) {
            if(child.getRank()==2 && child!=this) child = E;
            else getMembers()[index] = E;
        }

        public long getWMin() {
            if(child.getRank()==2 && child!=this) return child.getWMin();

            Element[] children = getMembers();
            long wMin = Long.MAX_VALUE;
            for (Element child : children) if(child.getWMin()<wMin) wMin=child.getWMin();

            return wMin;
        }

        public long getWMax() {
            if(child.getRank()==2 && child!=this) return child.getWMax();

            Element[] children = getMembers();
            long wMax = Long.MIN_VALUE;
            for (Element child : children) if(child.getWMax()>wMax) wMax=child.getWMax();

            return wMax;
        }

        public long getRMin() {
            if(child.getRank()==2 && child!=this) return child.getRMin();

            Element[] children = getMembers();
            long rMin = Long.MAX_VALUE;
            for (Element child : children) if(child.getRMin()<rMin) rMin=child.getRMin();

            return rMin;
        }

        public long getRMax() {
            if(child.getRank()==2 && child!=this) return child.getRMax();

            Element[] children = getMembers();
            long rMax = Long.MIN_VALUE;
            for (Element child : children) if(child.getRMax()>rMax) rMax=child.getRMax();

            return rMax;
        }

        public long getSpan() {
            return scheme.getLength()-1;
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
            Bin bin = Bin.lowestCommonAncestor(sBin,xBin);
            Segment v;
            // Is x a segment, or a match/insert?
            if(x.getRank()==3) v = (Segment) x;
            else {v=bin.newSegment();v.setChild(x);x.setParent(v);}

            // Insertion at head
            if(bin==sBin){Element temp=s.getChild();s.setChild(v);v.setParent(temp); return s;}

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
