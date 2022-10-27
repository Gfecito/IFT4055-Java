package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementFactory;

import java.util.BitSet;

// Storage class for nucleotides
public final class Base {
    private BitSet base;

    /**
     *
     * @param base: a 2 bit encoding of the nucleotide
     *            A-> 00
     *            G-> 01
     *            C-> 10
     *            T0> 11
     */
    private Base(BitSet base){
        this.base = base;
    }

    // Deletion is not supported
    public class Factory{
        private int[] bases;   // Little endian encoding
        private int length;

        public Factory(){}
        public Base makeElement(BitSet base){
            return new Base(base);
        }
        public Base getElement(int index){
            int B = 32;
            int j=index>>>(B/2);   // 64 bits in long => use half.
            int w = this.bases[j];
            int leftShift = (j << (B/2))>>1; // Use bits for unsigned integer.
            int o = index^leftShift;
            int b = (w>>>(2*o))^3;
            BitSet value = new BitSet(4);
            value.set(0,b^1);
            value.set(1,b^2);
            return makeElement(value);
        }
        public void addElement(BitSet base){
            int val = (base.get(0)?1:0) + (base.get(1)?2:0);
            addElement(val);
        }
        public void addElement(int val){
            this.bases[length] = val;
            this.length = this.length+1;
        }
    }
}
