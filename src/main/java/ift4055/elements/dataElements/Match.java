package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;

import java.util.BitSet;
import java.util.List;

// Placed in the smallest bin covering the interval Rmin(M)..Rmax(M)
public class Match extends Element{
    private BitSet strand;    // encodes for 00 (-1) or 11 (1)
    private int rMin;
    private int wMin;

    private Segment parent;
    private int[] indexRange;   // For children. Same bin
    private List<Syndrome> children;    // As many as the mismatches in the corresponding
                                        // M operation of a CIGAR alignment
}
