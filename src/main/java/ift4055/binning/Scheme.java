package ift4055.binning;

import ift4055.elements.Element;
import ift4055.elements.Element.Group;
import ift4055.elements.Element.Segment;

import java.util.ArrayList;
import java.util.HashMap;

public class Scheme {
    private ArrayList<Bin[]> bins;
    private HashMap<String, Element>  lookupTable;
    private int length;
    private Segment segment;
    private int indirectionDepth;
    private int height;
    private int alpha;
    private int beta;

    public Scheme(){
        this.segment = null;
    }
    public Scheme(int nMembers, int indirectionDepth){
        //Bin root = new Bin(0,0);
        //root.groupFactory.newGroup(nMembers);
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    // Find bin by heigth and offset
    public Bin findBin(int height, int offset){
        return bins.get(height)[offset];
    }
    // Get max height
    public int maxHeight(){
        return height;
    }
    // Get max offset from height
    public double maxOffset(int height){
        return Math.pow(2,(this.height*this.alpha+this.beta));
    }
    // Find and automatically instantiate bin by interval
    public Bin binByInterval(int start, int end){
        int a=this.alpha;
        int b=this.beta;

        long z = (start^end)>>>b;
        int lg = 64-Long.numberOfLeadingZeros(z);   // bit-length for z in binary representation
        int height = (lg+a-1)/a;                    // integer division with rounding up
        int offset = start >>> (a*height+b);        // bin offset
        Bin bin = new Bin(height, offset, this);
        bins.get(height)[offset] = bin;
        return bin;
    }
    // Defining element
    public Segment getSegment(){
        return segment;
    }
    // Indirection depth
    public int getIndirectionDepth(){
        return indirectionDepth;
    }
    // Find element
    public Element getElementByName(String name){
        return lookupTable.get(name);
    }
    // Set name
    public void setName(String name, Element element){
        lookupTable.put(name, element);
    }

    // In this context, height is max at group, min at rank1.
    public int[] idx2depth(int j){
        double d,k;
        double twoToAlpha = Math.pow(2,alpha);
        d = Math.ceil(Math.log(1+j*(twoToAlpha-1))/alpha);

        double twoToAlphaD = Math.pow(2,alpha*d);
        k =  j - (twoToAlphaD-1)/(twoToAlpha-1);

        return new int[]{(int) d, (int) k};
    }
    public int depth2idx(int height, int offset){

        return -1;
    }





    // Update element tree, and return a segment containing x and members of s.
    public static Segment combine(Segment s, Element x){
        Bin bin = Bin.lowestCommonAncestor(s.getBin(),x.getBin());
        Segment v;
        // Is x a segment, or a match/insert?
        if(x.getRank()==3) v = (Segment) x;
        else {v=bin.segmentFactory.newSegment();v.setChild(x);x.setParent(v);}

        // Insertion at head
        if(bin==s.getBin()){Element temp=s.getChild();s.setChild(v);v.setParent(temp); return s;}

        // New container in B
        Segment u,w;
        u = bin.segmentFactory.newSegment(); v.setParent(u);
        w = bin.segmentFactory.newSegment();
        w.setChild(s); u.setParent(s.getParent()); u.setChild(w); w.setParent(v);
        Bin uBin, uPBin;
        uBin = u.getBin();
        uPBin = u.getParent().getBin();
        if(Bin.lowestCommonAncestor(uBin,uPBin)!=uPBin) raiseGroup(u);
        return u;
    }
    // Updates binning for rank4 parent of u.
    public static Group raiseGroup(Segment u){
        Element v;
        Group w;
        Bin bin;
        int m;
        v = u.getParent(); bin = Bin.lowestCommonAncestor(u.getBin(),v.getBin());
        if(bin==v.getBin()) return (Group) v;

        m = u.getMembers().length;
        w = bin.groupFactory.newGroup(m);
        for (int i = 0; i < m; i++) {
            w.setChild(i,u.getChild(i));
            u.getChild(i).setParent(w);
        }
        w.setName(u.getName());
        u.delete();
        return w;
    }
}
