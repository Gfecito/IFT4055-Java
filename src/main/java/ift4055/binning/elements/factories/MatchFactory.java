package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.*;
import ift4055.binning.elements.Factory;

public class MatchFactory implements Factory.Match{
    private Match[] objects;
    private int index;
    Bin bin;

    public MatchFactory(Bin bin){
        objects = new Match[16];
        index = 0;
        this.bin = bin;
    }

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Match[] newObjects = new Match[c];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);

        this.objects = newObjects;
    }

    public Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset) {
        Segment parent = Bin.ref(bin);
        // Add children
        for (int i = 0; i < span; i++)
            bin.newSyndrome(dnaSequence[i+offset], rMin+i);

        Match match = new Match(strand, rMin, span, wMin, parent);
        if(index >= objects.length) expandCapacity();
        objects[index] = match;
        index++;
        return match;
    }

    // M operation of a CIGAR alignment
    public class Match implements Element.Match{
        private Segment parent;
        private final int strand;
        private final int rMin;
        private final int span;
        private final int wMin;
        private Match(int strand, int rMin, int span, int wMin, Segment parent){
            this.strand = strand;
            this.rMin = rMin;
            this.span = span;
            this.wMin = wMin;
            this.parent = parent;
        }

        public Bin getBin(){
            return bin;
        }

        public Element getParent(int index){
            return parent;
        }
        public Element getChild(int index){
            return getMembers()[index];
        }

        public Element[] getMembers() {
            Syndrome[] binSyndromes = bin.getSyndromes();
            Syndrome[] children = new Syndrome[span];
            System.arraycopy(binSyndromes, rMin, children, 0, span);

            return children;
        }


        public long getWMin() {
            return wMin;
        }

        public long getWMax() {
            return wMin + getLength();
        }

        public long getRMin() {
            return rMin;
        }

        public long getRMax() {
            return rMin + span*getLength();
        }

        public long getSpan() {
            return span;
        }

        public int getStrand() {
            return strand;
        }


        // DNA sequences
        // Search recursively
        public Base getNucleotideAt(int index){
            return getChild(index).getNucleotideAt(0);
        }

        public void setParent(Element E) {
            parent = (Segment) E;
        }

        // Deletion
        public void delete(){ parent = null; }
    }
}