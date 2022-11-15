package ift4055.binning;

import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.*;
import ift4055.binning.elements.factories.*;

import java.util.HashSet;

public class Bin {
    // These attributes need a revision. TODO
    private final int offset;
    private final int height;
    private final int index;
    private final Scheme container;
    // Do these matter?
    private String annotatedGenomicPosition;
    private int[][] intervals;
    private HashSet<int[]>[] intervalSets;


    public Bin(int height, int offset, Scheme scheme){
        this.height = height;
        this.offset = offset;
        this.container = scheme;
        index = scheme.depth2idx(height, offset);
    }
    /**
     * Factories
     */
    private BaseFactory baseFactory = new BaseFactory(this);
    private SyndromeFactory syndromeFactory = new SyndromeFactory(this);
    private InsertFactory insertFactory = new InsertFactory(this);
    private MatchFactory matchFactory = new MatchFactory(this);
    private SegmentFactory segmentFactory = new SegmentFactory(this);
    private GroupFactory groupFactory = new GroupFactory(this);
    private ConnectorFactory connectorFactory = new ConnectorFactory(this);
    private EdgeFactory edgeFactory = new EdgeFactory(this);
    private NodeFactory nodeFactory = new NodeFactory(this);


    public Base addBase(int b){
        return baseFactory.addBase(b);
    }
    public Syndrome newSyndrome(int syndrome, int readPosition){
        return syndromeFactory.newSyndrome(syndrome, readPosition);
    }
    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        return insertFactory.newInsert(strand, rMin, span, wMin, dnaSequence, offset);
    }
    public Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        return matchFactory.newMatch(strand, rMin, span, wMin, dnaSequence, offset);
    }
    public Segment newSegment(){
        return segmentFactory.newSegment();
    }
    public Group newGroup(int nMembers){
        return groupFactory.newGroup(nMembers);
    }

    public Connector newConnector(){
        return connectorFactory.newConnector();
    }
    public Edge newEdge(int start, int span){
        return edgeFactory.newEdge(start, span);
    }
    public Node newNode(){
        return nodeFactory.newNode();
    }




    /**
     * Find the closest common ancestor
     */
    public static int idepth(Bin a){
        return a.container.getIndirectionDepth();
    }
    public static Segment ref(Bin a){
        return a.container.getSegment();
    }
    // How exactly should I get a bin from the reference element of its container?
    // Why would this be a different bin than the previous, or have a lower indirection
    // depth? Would it be the bin with the segment that was the parent? That makes sense.
    // Ok so how do I find that?
    private static Bin bin(Segment s){
        // Here I get the parent, easy
        Element parent = s.getParent();
        // Get the bin!
        return parent.getBin();
    }
    public static Bin lowestCommonAncestor(Bin a, Bin b){
        int alpha, beta, j, k, d, h, z, g, e, i;
        while(idepth(a)>idepth(b)) a = bin(ref(a));
        while(idepth(a)<idepth(b)) b = bin(ref(b));
        while(ref(a)!=ref(b)) {a = bin(ref(a)); b = bin(ref(b));}
        // Now a and b are in the same binning scheme
        Scheme shared = a.container;
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
