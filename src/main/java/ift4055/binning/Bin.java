package ift4055.binning;

import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.elements.Element.*;
import ift4055.binning.elements.factories.*;

public class Bin {
    private final int offset;
    private final int height;
    private final int index;
    private final Scheme container;


    public Bin(int depth, int height, int offset, Scheme scheme){
        this.height = height;
        this.offset = offset;
        this.container = scheme;
        index = scheme.depth2idx(depth, offset);
        if(index<0) throw new RuntimeException("Negative index in bin???");
    }

    /**
     *
     * @return this bin's scheme
     */
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


    /**
     *
     * @param b the integer representation of the base to add to bin.
     * @return b wrapped as a Base instance.
     */
    public Base addBase(int b){
        if(baseFactory==null) baseFactory = new BaseFactory(this);
        return baseFactory.addBase(b);
    }

    /**
     *
     * @param syndrome strand encoding.
     * @param readPosition the position that corresponds to this match's syndromes.
     * @return the syndrome added to the bin.
     */
    public Syndrome newSyndrome(int syndrome, int readPosition){
        if(syndromeFactory==null) syndromeFactory = new SyndromeFactory(this);
        return syndromeFactory.newSyndrome(syndrome, readPosition);
    }

    /**
     *
     * @param strand DNA sequence strand.
     * @param rMin lowest read position.
     * @param span span of insert.
     * @param wMin starting writing position.
     * @param dnaSequence a sequence of nucleotides which might go further than just this insert. Like a stream.
     *                    If not, just set offset to 0.
     * @param offset the start of the encoded DNA sequence of this insert, which is also added to this bin.
     * @return the insert added to the bin.
     */
    public Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        if(insertFactory==null) insertFactory = new InsertFactory(this);
        return insertFactory.newInsert(strand, rMin, span, wMin, dnaSequence, offset);
    }
    /**
     *
     * @param strand DNA sequence strand.
     * @param rMin lowest read position.
     * @param span span of match.
     * @param wMin starting writing position.
     * @param dnaSequence a sequence of nucleotides which might go further than just this match. Like a stream.
     *                    If not, just set offset to 0.
     * @param offset the start of the encoded DNA sequence of this match, which is also added to this bin.
     * @return the match added to the bin.
     */
    public Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset){
        if(matchFactory==null) matchFactory = new MatchFactory(this);
        return matchFactory.newMatch(strand, rMin, span, wMin, dnaSequence, offset);
    }

    /**
     *
     * @return the segment added to this bin.
     */
    public Segment newSegment(){
        if(segmentFactory==null) segmentFactory = new SegmentFactory(this);
        return segmentFactory.newSegment();
    }

    /**
     *
     * @param nMembers the number of contigs in this group
     * @return the group added to this bin.
     */
    public Group newGroup(int nMembers){
        if(groupFactory==null) groupFactory = new GroupFactory(this);
        return groupFactory.newGroup(nMembers);
    }

    /**
     *
     * @return the connector added to this bin.
     */
    public Connector newConnector(){
        if(connectorFactory==null) connectorFactory = new ConnectorFactory(this);
        return connectorFactory.newConnector();
    }

    /**
     *
     * @param start start of fragment covered by edge.
     * @param span span of fragment covered by edge.
     * @return the edge added to this bin.
     */
    public Edge newEdge(int start, int span){
        if(edgeFactory==null) edgeFactory = new EdgeFactory(this);
        return edgeFactory.newEdge(start, span);
    }

    /**
     *
     * @return the node added to this bin.
     */
    public Node newNode(){
        if(nodeFactory==null) nodeFactory = new NodeFactory(this);
        return nodeFactory.newNode();
    }


    /**
     *
     * @return all the non-null syndromes in this bin.
     */
    public Syndrome[] getSyndromes(){
        return syndromeFactory.getSyndromes();
    }
    /**
     *
     * @return all the non-null bases in this bin.
     */
    public Base[] getBases(){
        return baseFactory.getBases();
    }
    /**
     *
     * @return all the non-null inserts in this bin.
     */
    public Insert[] getInserts(){
        if(insertFactory==null) return new Insert[0];
        return insertFactory.getInserts();
    }
    /**
     *
     * @return all the non-null matches in this bin.
     */
    public Match[] getMatches(){
        if(matchFactory==null) return new Match[0];
        return matchFactory.getMatches();
    }

    /**
     *
     * @return all the non-null segments in this bin.
     */
    public Segment[] getSegments(){
        if(segmentFactory==null) return new Segment[0];
        return segmentFactory.getSegments();
    }

    /**
     *
     * @return all the non-null connectors in this bin.
     */
    public Connector[] getConnectors(){
        if(connectorFactory==null) return new Connector[0];
        return connectorFactory.getConnectors();
    }
    /**
     *
     * @return all the non-null edges in this bin.
     */
    public Edge[] getEdges(){
        if(edgeFactory==null) return new Edge[0];
        return edgeFactory.getEdges();
    }
    /**
     *
     * @return all the non-null nodes in this bin.
     */
    public Node[] getNodes(){
        if(nodeFactory==null) return new Node[0];
        return nodeFactory.getNodes();
    }

    /**
     *
     * @param a the bin.
     * @return the count of indirect links necessary to reach this bin from root.
     */
    public static int idepth(Bin a){
        return a.container.getIndirectionDepth();
    }

    /**
     *
     * @param a the bin.
     * @return the reference segment of the bin's scheme.
     */
    public static Segment ref(Bin a){
        return a.container.getSegment();
    }

    /**
     * Syntax sugar for s.getBin() for segments
     * @param s the segment.
     * @return its bin.
     */
    private static Bin bin(Segment s){
        return s.getBin();
    }

    /**
     *
     * @param a a bin at some coordinates.
     * @param b a bin at potentially different coordinates, or even scheme.
     * @return the lowest bin covering both a and b.
     */
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



    /*
    AFFICHAGE
    */
    @Override
    public String toString(){
        Insert[] inserts = getInserts();
        Match[] matches = getMatches();
        String bin = "The following inserts:\n";
        for(Insert insert: inserts) bin += insert.toString();

        bin += "And the following matches:\n";
        for (Match match : matches) bin += match.toString();

        return bin;
    }
}
