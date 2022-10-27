package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;

import java.util.BitSet;

// Shares bin with Match
public class Syndrome extends Element {
    private int readPosition;
    private BitSet syndrome;

    private Syndrome(BitSet syndrome, int readPosition) {
        this.readPosition = readPosition;
        this.syndrome = syndrome;
    }

    public class Factory {
        private int[] syndromes;   // Little endian encoding
        private int length;

        public Factory() {}

        public Syndrome makeElement(BitSet syndrome, int readPosition) {
            return new Syndrome(syndrome, readPosition);
        }

        public Syndrome getElement(int index) {
            int syndromeEncoded = this.syndromes[index];
            BitSet syndrome = new BitSet(4);
            syndrome.set(0,syndromeEncoded^1);
            syndrome.set(1,syndromeEncoded^2);
            int syndromeRemoved = syndromeEncoded >>> 2;    // Remove 2 smallest bits.
            int readPosition = (syndromeEncoded << 2)>>1;   // Unsigned 2-bit left shift recovers the n-2 biggest bits

            return makeElement(syndrome,readPosition);
        }

        public void addElement(BitSet syndrome, int readPosition) {
            int val = (syndrome.get(0)?1:0) + (syndrome.get(1)?2:0);;
            val += (readPosition<<2)>>>1;   // Saves in biggest bits
            addElement(val);
        }

        public void addElement(int val) {
            this.syndromes[length] = val;
            this.length = this.length + 1;
        }
    }
}