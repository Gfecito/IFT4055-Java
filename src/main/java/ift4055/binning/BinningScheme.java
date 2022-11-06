package ift4055.binning;
import ift4055.elements.dataElements.Segment;
import ift4055.interfaces.Element;

import java.util.HashMap;

public class BinningScheme {
    private Bin[][] bins;
    private HashMap<String, Element>  lookupTable;
    private int length;
    private Segment segment;
    private int indirectionDepth;
    private int height;
    private int alpha;
    private int beta;


    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    // Find bin by heigth and offset
    public Bin findBin(int height, int offset){
        return bins[height][offset];
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
        Bin bin = new Bin(height, offset);
        bins[height][offset] = bin;
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
}
