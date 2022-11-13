package ift4055.binning;

import ift4055.elements.Element;
import ift4055.elements.Element.Segment;

import java.util.HashMap;

public class Scheme {
    private long l;
    private Segment segment;
    private int maximumHeight;
    private Bin[][] bins;
    private HashMap<String, Element>  lookupTable = new HashMap<>();
    private int alpha;
    private int beta;

    // Root
    public Scheme(){
        segment = null;
        maximumHeight = 0;
    }
    public Scheme(Segment segment){
        l = segment.getLength();
        this.segment = segment;
    }

    public void addNamedElement(String name, Element e){
        lookupTable.put(name, e);
    }
    public Element getNamedElement(String name){
        return lookupTable.get(name);
    }

    /* PRINCIPAL OPERATIONS */
    // Find bin by height and offset
    public Bin findBin(int height, int offset){
        return bins[height][offset];
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    // Get max height
    public int maxHeight(){
        return -1;
    }
    // Get max offset from height
    public double maxOffset(int height){
        return Math.pow(2,(height*this.alpha+this.beta));
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
        bins[height][offset] = bin;
        return bin;
    }
    // Defining element
    public Segment getSegment(){
        return segment;
    }
    // Indirection depth
    public int getIndirectionDepth(){
        return -1;
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

        return new int[]{ (int) d, (int) k};
    }
    public int depth2idx(int depth, int offset){
        return (int) ((Math.pow(2,alpha*depth)-1)/(Math.pow(2,alpha)-1)+offset);
    }
}
