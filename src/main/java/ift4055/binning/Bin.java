package ift4055.binning;

import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.*;
import ift4055.binning.elements.factories.*;

public class Bin {
    // These attributes need a revision. TODO
    private final int offset;
    private final int height;
    private final int index;
    private final Scheme container;


    public Bin(int height, int offset, Scheme scheme){
        this.height = height;
        this.offset = offset;
        this.container = scheme;
        index = scheme.depth2idx(height, offset);
        if(index<0) throw new RuntimeException("Negative index in bin???");
    }
    public Scheme getScheme(){
        return container;
    }

    /**
     * Factories
     */
    private BaseFactory baseFactory;
    private SyndromeFactory syndromeFactory;
    private InsertFactory insertFactory;
    private MatchFactory matchFactory;
    private SegmentFactory segmentFactory;
    private GroupFactory groupFactory;
    private ConnectorFactory connectorFactory;
    private EdgeFactory edgeFactory;
    private NodeFactory nodeFactory;


    public Base addBase(int b){
        if(baseFactory==null) baseFactory = new BaseFactory(this);
        return baseFactory.addBase(b);
    }
    public Syndrome newSyndrome(int syndrome, int readPosition){
        if(syndromeFactory==null) syndromeFactory = new SyndromeFactory(this);
        return syndromeFactory.newSyndrome(syndrome, readPosition);
    }
    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        if(insertFactory==null) insertFactory = new InsertFactory(this);
        return insertFactory.newInsert(strand, rMin, span, wMin, dnaSequence, offset);
    }
    public Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        if(matchFactory==null) matchFactory = new MatchFactory(this);
        return matchFactory.newMatch(strand, rMin, span, wMin, dnaSequence, offset);
    }
    public Segment newSegment(){
        if(segmentFactory==null) segmentFactory = new SegmentFactory(this);
        return segmentFactory.newSegment();
    }
    public Group newGroup(int nMembers){
        if(groupFactory==null) groupFactory = new GroupFactory(this);
        return groupFactory.newGroup(nMembers);
    }

    public Connector newConnector(){
        if(connectorFactory==null) connectorFactory = new ConnectorFactory(this);
        return connectorFactory.newConnector();
    }
    public Edge newEdge(int start, int span){
        if(edgeFactory==null) edgeFactory = new EdgeFactory(this);
        return edgeFactory.newEdge(start, span);
    }
    public Node newNode(){
        if(nodeFactory==null) nodeFactory = new NodeFactory(this);
        return nodeFactory.newNode();
    }



    // Get nucleotides
    public Syndrome[] getSyndromes(){
        return syndromeFactory.getSyndromes();
    }
    public Base[] getBases(){
        return baseFactory.getBases();
    }
    public Insert[] getInserts(){
        if(insertFactory==null) return new Insert[0];
        return insertFactory.getInserts();
    }
    public Match[] getMatches(){
        if(matchFactory==null) return new Match[0];
        return matchFactory.getMatches();
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
        int alpha, j, k, d, h, z, g, e, i;
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
        if(d>=0) {h = a.height; k = k>>>(d*alpha);}    // Bit shifting wizardry
        else {h=b.height; j=j>>>(-d*alpha);}
        z = j^k;
        g = 32 - Integer.numberOfLeadingZeros(z);      // Using 32-bit integers
        e = (int) (Math.ceil((g+alpha-1.0)/(float) alpha));         // Ceiling with integer division
        i = j>>>(e*alpha);
        h += e;

        return shared.findBin(i);
    }
}
