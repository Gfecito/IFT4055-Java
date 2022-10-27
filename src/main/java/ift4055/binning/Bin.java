package ift4055.binning;

import java.util.HashSet;

public class Bin {
    private int offset;
    private int height;
    private int index;
    private String annotatedGenomicPosition;
    private int[][] intervals;
    private HashSet<int[]>[] intervalSets;
}
