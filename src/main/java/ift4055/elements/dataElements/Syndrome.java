package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;
import ift4055.interfaces.ranks.Rank1;

import java.util.BitSet;

// Shares bin with Match
public class Syndrome implements Rank1 {
    private int readPosition;
    private int syndrome;

    private Syndrome(int syndrome, int readPosition) {
        this.readPosition = readPosition;
        this.syndrome = syndrome;
    }

    public class Factory {
        private int[] syndromes;   // Little endian encoding
        private int length;

        public Factory() {}

        public Syndrome makeElement(int syndrome, int readPosition) {
            return new Syndrome(syndrome, readPosition);
        }

        public Syndrome getElement(int index) {
            int syndromeEncoded = this.syndromes[index];
            int syndrome = syndromeEncoded&3;
            int readPosition = (syndromeEncoded-syndrome)/4;

            return makeElement(syndrome,readPosition);
        }

        public void addElement(int syndrome, int readPosition) {
            int val = syndrome;             // Save in 2 smallest bits
            val += readPosition*4;          // Save in 30 biggest bits
            addElement(val);
        }

        public void addElement(int val) {
            this.syndromes[this.length] = val;
            this.length = this.length + 1;
        }
    }
}