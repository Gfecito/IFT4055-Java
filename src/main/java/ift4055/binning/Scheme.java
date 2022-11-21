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
    private int alpha = 2;
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
        height = maximumHeight = maxHeight();
        height++;
        bins = new Bin[height][];
        for (int i = 0; i < height; i++) {
            int depth = height - i;
            width = maxOffset(i);
            bins[i] = new Bin[width];
            for (int j = 0; j < width; j++) bins[i][j] = new Bin(height-i,j,this);
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
    public Bin findBin(int index){
        int[] depthAndOffset = idx2depth(index);
        int depth = depthAndOffset[0];
        int offset = depthAndOffset[1];
        return findBin(maximumHeight-depth, offset);
    }
    public Bin findBin(int height, int offset){
        int depth = maximumHeight-height;
        Bin bin = bins[depth][offset];
        if(bin==null) bin = new Bin(height,offset,this);
        bins[depth][offset] = bin;
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
        return coveringHeight(start, end);
    }
    public int coveringHeight(int start, int end){     // Max for section
        int a=alpha;
        int b=beta;

        long z = (start^end)>>>b;
        int lg = 64-Long.numberOfLeadingZeros(z);   // bit-length for z in binary representation
        int height = (lg+a-1)/a;                    // integer division with rounding up
        return height;
    }
    // Get max offset from height
    public int maxOffset(int height){
        return 1<<(height*alpha+beta);
    }
    // Find and automatically instantiate bin by interval
    public Bin coveringBin(int start, int end){
        int height = coveringHeight(start, end);
        int offset = start >>> (alpha*height+beta);      // bin offset

        return findBin(height,offset);
    }
    // Defining element
    public Segment getSegment(){
        return segment;
    }
    // Indirection depth
    public int getIndirectionDepth(){
        if(segment==null) return 0;
        if(segment.getParent().equals(segment)) return 0;
        return 1+((Segment) segment.getParent()).getScheme().getIndirectionDepth();
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
        int d,k;
        int twoToAlpha = 1<<alpha;
        //d = (int) (Math.log(1+j*(twoToAlpha-1))/(float)alpha);
        d = 1;
        while((1<<alpha*d)<j) d++;

        int twoToAlphaD = twoToAlpha << d;
        k =  j - (twoToAlphaD-1)/(twoToAlpha-1);

        return new int[]{ (int) d, (int) k};
    }
    public int depth2idx(int depth, int offset){
        return (1<<(alpha*depth)-1)/(1<<alpha-1)+offset;
    }
}
