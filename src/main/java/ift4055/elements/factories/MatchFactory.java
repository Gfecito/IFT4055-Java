package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Element.Segment;
import ift4055.elements.Factory;

import java.util.ArrayList;
import java.util.List;

public class MatchFactory implements Factory.Match{
    private final List<Match> objects;
    Bin bin;

    public MatchFactory(Bin bin){
        objects = new ArrayList<>();
        this.bin = bin;
    }

    public Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence) {
        Segment parent = Bin.ref(bin);
        Match match = new Match(strand, rMin, span, wMin, parent);
        // Add children
        objects.add(match);
        return match;
    }

    // M operation of a CIGAR alignment
    public class Match implements Element.Match{
        private Segment parent;
        private final int strand;
        private final int rMin;
        private final int span;
        private final int wMin;
        private Syndrome[] children;
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
            return children[index];
        }

        public Element[] getMembers() {
            return children;
        }


        public int getWMin() {
            return wMin;
        }

        public int getWMax() {
            return wMin + span;
        }

        public int getRMin() {
            return rMin;
        }

        public int getRMax() {
            return rMin + span*strand;
        }

        public int getLength() {
            return span+1;
        }

        public int getSpan() {
            return span;
        }

        public int getStrand() {
            return strand;
        }


        // DNA sequences
        public Base getNucleotideAt(int index){
            Element container = getChild(index/16);
            return container.getNucleotideAt(index%16);
        }

        public void setParent(Element E) {
            parent = (Segment) E;
        }

        // Deletion
        public void delete(){ parent = null; }
    }
}