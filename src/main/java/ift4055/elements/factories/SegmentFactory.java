package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Factory;

import java.util.ArrayList;
import java.util.List;

public class SegmentFactory implements Factory.Segment {
    private final List<Segment> objects;
    Bin bin;

    SegmentFactory(Bin b)
    {
        objects = new ArrayList<>();
        this.bin = b;

        Segment sentinel = new Segment();
        sentinel.parent = sentinel.child = sentinel;

        objects.add(sentinel);
    }

    @Override
    public Segment newSegment()
    {
        Segment sentinel = objects.get(0);
        if (sentinel.child == sentinel)
        {
            // if no empty then extend
            Segment s = new Segment();
            s.parent = s.child = s;
            objects.add(s);
            return s;
        } else
        {
            Segment s = (Segment) sentinel.child;
            sentinel.child = s.child;
            s.parent=s.child=s;
            return s;
        }
    }

    private class Segment implements Element.Segment {
        private Element parent;
        private Element child;
        private String name;



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
            if(child!=null){ Element[] members = {child}; return members;}
            else{ Element[] members = {}; return members;}
        }

        public void setChild(int index, Element E) {
            child = E;
        }

        public int getWMin() {
            return child.getWMin();
        }

        public int getWMax() {
            return child.getWMax();
        }

        public int getRMin() {
            return child.getRMin();
        }

        public int getRMax() {
            return child.getRMax();
        }


        public Base getNucleotideAt(int index) {
            // There's an offset here probably TODO
            return child.getNucleotideAt(index);
        }


        public void delete() {
            parent = null;
            child = null;
        }


        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
