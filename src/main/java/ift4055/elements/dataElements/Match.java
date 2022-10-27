package ift4055.elements.dataElements;

import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;

import java.util.BitSet;
import java.util.List;

// Placed in the smallest bin covering the interval Rmin(M)..Rmax(M)
public class Match extends Element {
    private BitSet strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;

    private Segment parent;
    private int[] indexRange;   // For children. Same bin
    private List<Syndrome> children;    // As many as the mismatches in the corresponding. Unused for now

    public void setParent(Segment parent) {
        this.parent = parent;
    }

    private Match(BitSet strand, int rMin, int wMin, Segment parent, int[] indexRange){
        this.strand = strand;
        this.rMin = rMin;
        this.wMin = wMin;
        this.parent = parent;
        this.indexRange = indexRange;
    }

    // M operation of a CIGAR alignment
    public class Factory {
        private Match[] matches;
        private int length;

        public Match getElement(int i) {
            return this.matches[i];
        }

        public void makeElement(BitSet strand, int rMin, int wMin, Segment parent, int[] indexRange) {
            this.matches[this.length] = new Match(strand, rMin, wMin, parent, indexRange);
            this.length = this.length + 1;
        }

        public void deleteElement(int i) {
            Match match = this.matches[i];
            match.setParent(null);
        }
    }
}
