package ift4055.binning;

import ift4055.elements.dataElements.*;
import java.util.HashSet;

public class Bin {
    private int offset;
    private int height;
    private int index;
    private BinningScheme container;
    private String annotatedGenomicPosition;
    private int[][] intervals;
    private HashSet<int[]>[] intervalSets;


    public Bin(int height, int offset){
        this.height = height;
        this.offset = offset;
    }
    /**
     * Factories
     */
    public Base.Factory baseFactory;
    public Syndrome.Factory syndromeFactory;
    public Insert.Factory insertFactory;
    public Match.Factory matchFactory;
    public Segment.Factory segmentFactory;
    public Group.Factory groupFactory;


    /**
     * Methods
     */

    /**
     * Find least common ancestor
     */
    private static int idepth(Bin a){
        return a.container.getIndirectionDepth();
    }
    private static Segment ref(Bin a){
        return a.container.getSegment();
    }
    // How exactly should I get a bin from the reference element of its container?
    // Why would this be a different bin than the previous, or have a lower indirection
    // depth? Would it be the bin with the segment that was the parent? That makes sense.
    // Ok so how do I find that?
    private static Bin bin(Segment s){
        // Here I get the parent, easy
        Segment.SegmentParent parent = s.getParent();
        // Get the bin!
        return parent.getBin();
    }
    public static Bin lowestCommonAncestor(Bin a, Bin b){
        int alpha, beta, j, k, d, h, z, g, e, i;
        while(idepth(a)>idepth(b)) a = bin(ref(a));
        while(idepth(a)<idepth(b)) b = bin(ref(b));
        while(ref(a)!=ref(b)) {a = bin(ref(a)); b = bin(ref(b));}
        // Now a and b are in the same binning scheme
        BinningScheme shared = a.container;
        alpha = shared.getAlpha();
        // Bin indexes
        j = a.index;
        k = b.index;
        // Height difference
        d = a.height - b.height;
        // H is the maximum height
        if(0<=d) {h = a.height; k = k>>>(d*alpha);}    // Bit shifting wizardry
        else {h=b.height; j=j>>>(-d*alpha);}
        z = j^k;
        g = 32 - Integer.numberOfLeadingZeros(z);      // Using 32-bit integers
        e = (g+alpha-1)/alpha;
        i = j>>>(e*alpha);
        h += e;

        return shared.findBin(h,i);
    }
}
