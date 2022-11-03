package ift4055.binning;

import ift4055.elements.dataElements.*;

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
     * Factories
     */
    private Base.Factory baseFactory;
    private Syndrome.Factory syndromeFactory;
    private Insert.Factory insertFactory;
    private Match.Factory matchFactory;
    private Segment.Factory segmentFactory;
    private Group.Factory groupFactory;
}
