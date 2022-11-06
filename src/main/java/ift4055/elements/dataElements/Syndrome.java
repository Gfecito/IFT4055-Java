package ift4055.elements.dataElements;
import ift4055.interfaces.ranks.Rank1;

// Shares bin with Match
public class Syndrome implements Rank1 {
    private int readPosition;
    private int syndrome;

    private Syndrome(int syndrome, int readPosition) {
        this.readPosition = readPosition;
        this.syndrome = syndrome;
    }

    public int getSyndrome() {
        return syndrome;
    }

    public int getReadPosition() {
        return readPosition;
    }

    public class Factory {
        private int[] syndromes;   // Little endian encoding
        private int length;

        public Factory() {}

        public int addSyndrome(int readPosition, int syndrome) {
            int index = this.length;        // Add at the end
            int val = syndrome;             // Save in 2 smallest bits
            val += readPosition*4;          // Save in 30 biggest bits
            this.syndromes[index] = val;
            this.length++;
            return index;
        }
    }
}