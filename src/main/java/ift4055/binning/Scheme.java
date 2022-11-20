package ift4055.binning;

import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.Segment;

import java.util.HashMap;

public class Scheme {
    private long l;
    private Segment segment;
    private int maximumHeight;
    private Bin[][] bins;
    private HashMap<String, Element>  lookupTable = new HashMap<>();
    private int alpha = 4;
    private int beta = 0;

    // Root scheme.
    // Everything else will be an indirect child
    public Scheme(){
        bins = new Bin[1][1];
        bins[0][0] = new Bin(0,0,this);
        segment = null;
        maximumHeight = 0;
    }
    public Scheme(Segment segment, int length){
        l = length;
        this.segment = segment;

        int height, width;
        height = maxHeight();
        bins = new Bin[height][];
        for (int i = 0; i < height; i++) {
            width = (int) maxOffset(i);
            bins[i] = new Bin[width];
            for (int j = 0; j < width; j++) bins[i][j] = new Bin(i,j,this);
        }
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
        Bin bin = bins[height][offset];
        if(bin==null) bin = new Bin(height,offset,this);
        bins[height][offset] = bin;
        return bin;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    // Get max height
    public int maxHeight(){     // Max of scheme
        int start = 0;
        int end = (int) l;
        return maxHeight(start, end);
    }
    public int maxHeight(int start, int end){     // Max for section
        int a=alpha;
        int b=beta;

        long z = (start^end)>>>b;
        int lg = 64-Long.numberOfLeadingZeros(z);   // bit-length for z in binary representation
        int height = (lg+a-1)/a;                    // integer division with rounding up
        return height;
    }
    // Get max offset from height
    public double maxOffset(int height){
        return Math.pow(2,(height*alpha+beta));
    }
    // Find and automatically instantiate bin by interval
    public Bin coveringBin(int start, int end){
        int height = maxHeight(start, end);
        int offset = start >>> (alpha*height+beta);      // bin offset

        // If not there yet, instantiate on the go.
        if(bins[height][offset]==null) bins[height][offset] = new Bin(height, offset, this);

        return bins[height][offset];
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
