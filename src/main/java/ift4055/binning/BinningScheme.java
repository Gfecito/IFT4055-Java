package ift4055.binning;
import ift4055.elements.dataElements.Segment;

public class BinningScheme {
    private int height;
    private int alpha;
    private int beta;
    private Bin root;

    public Bin findBin(int height, int offset){
        Bin bin = new Bin();
        return bin;
    }

    public int maxHeight(){
        return this.height;
    }

    public double maxOffset(int height) throws IllegalArgumentException{
        if(height>this.height) throw new IllegalArgumentException();
        return Math.pow(2,(this.height*this.alpha+this.beta));
    }

    public Bin binByInterval(int[] interval){
        int s=interval[0];  // i
        int e=interval[1];  // i plus sigma
        int a=this.alpha;
        int b=this.beta;

        long z = (s^e)>>>b;
        int lg = 64-Long.numberOfLeadingZeros(z);  // bit-length for z in binary representation
        int h = (lg+a-1)/a;    // integer division with rounding up
        int k = s >>> (a*h+b); // bin offset
        Bin bin = new Bin();
        return bin;
    }

    public Bin getElementByName(String name){
        Bin bin = new Bin();
        return bin;
    }


}
