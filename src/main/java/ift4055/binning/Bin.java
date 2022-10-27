package ift4055.binning;

import ift4055.elements.dataElements.Base;

import java.util.BitSet;
import java.util.HashSet;

public class Bin {
    private int offset;
    private int height;
    private int index;
    private String annotatedGenomicPosition;
    private int[][] intervals;
    private HashSet<int[]>[] intervalSets;


    /**
     * Factories and bases
     */
    private Base.Factory baseFactory;
}
