package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.elements.Factory;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.Match;

public class SyndromeFactory implements Factory.Syndrome {
    Bin bin;
    Match parent;
    int[] objects;
    int length;
    public SyndromeFactory(Bin bin){
        this.bin = bin;
        this.parent = parent;
        objects = new int[16];
        this.length = 0;
    }

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        int[] newObjects = new int[c];
        for (int i = 0; i < objects.length; i++)
            newObjects[i] = objects[i];

        this.objects = newObjects;
    }

    public Syndrome newSyndrome(int syndrome, int readPosition) {
        if(length>= objects.length) expandCapacity();
        int index = length;        // Add at the end
        int val = syndrome;             // Save in 2 smallest bits
        val += readPosition*4;          // Save in 30 biggest bits
        objects[index] = val;
        length++;
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
        public long getWMin(){
            return readPosition;
        }

        public long getWMax(){
            return readPosition;
        }

        public long getRMin(){
            return readPosition;
        }

        public long getRMax(){
            return readPosition;
        }

        public long getSpan() {
            return 0;
        }

        // DNA sequences
        // Search recursively
        public Base getNucleotideAt(int index){
            Base b = Bin.ref(bin).getNucleotideAt(readPosition);
            return b.syndromize(syndrome);       // Modify this
        }
    }
}
