package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Element.Match;
import ift4055.elements.Factory;

public class SyndromeFactory implements Factory.Syndrome {
    Bin bin;
    Match parent;
    int[] objects;
    int length;
    public SyndromeFactory(Bin bin, Match parent){
        this.bin = bin;
        this.parent = parent;
        objects = new int[parent.getLength()];
        this.length = 0;
    }
    @Override
    public Syndrome newSyndrome(int syndrome, int readPosition) {
        int index = this.length;        // Add at the end
        int val = syndrome;             // Save in 2 smallest bits
        val += readPosition*4;          // Save in 30 biggest bits
        this.objects[index] = val;
        this.length++;
        return new Syndrome(syndrome, readPosition);
    }

    public class Syndrome implements Element.Syndrome{
        private final int syndrome;
        private final int readPosition;

        public Syndrome(int syndrome, int readPosition){
            this.syndrome = syndrome;
            this.readPosition = readPosition;
        }

        public Bin getBin(){
            return bin;
        }

        public Element getParent(int index){
            return parent;
        }

        public Element getChild(int index){
            return this;
        }


        // Genome coordinates
        public int getWMin(){
            return readPosition;
        }

        public int getWMax(){
            return readPosition;
        }

        public int getRMin(){
            return readPosition;
        }

        public int getRMax(){
            return readPosition;
        }

        // DNA sequences
        public Base getNucleotideAt(int index){
            throw new UnsupportedOperationException();
        }
    }
}
