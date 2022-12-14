package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.elements.Factory;
import ift4055.binning.elements.Element;

public class BaseFactory implements Factory.Base {
    private int[] bases;   // Little endian encoding
    private int nBases;
    private final Bin bin;

    public BaseFactory(Bin bin){
        bases = new int[16];
        this.bin = bin;
        nBases=0;
    }

    public Base addBase(int b){
        if(nBases >= bases.length/16) expandCapacity();
        int index = nBases;
        int B = 16;                     // 32 bits in int => 2 bits per base => 16 bases per int.
        int containerIndex =index/B;    // In which int?
        int containerAdress=index%B;    // Which bits in the int?
        int container = bases[containerIndex];
        container += (b<<(2*containerAdress));       // Save new base in this integer.

        bases[containerIndex] = container;
        nBases++;
        return new Base(b);
    }

    /**
     * Replace the current object array with a bigger one,
     * to be used whenever the previous one is filled.
     */
    private void expandCapacity(){
        int c = bases.length;
        c = c%3==0? 3*c/2: 4*c/3;
        int[] newObjects = new int[c];
        System.arraycopy(bases, 0, newObjects, 0, bases.length);

        this.bases = newObjects;
    }

    public Base[] getBases(){
        Base[] decodedBases = new Base[nBases];
        int i=0;
        while(i<nBases){
            int B = 16;                     // 32 bits in int => 2 bits per base => 16 bases per int.
            int containerIndex =i/B;    // In which int?
            int containerAdress=i%B;    // Which bits in the int?
            int container = bases[containerIndex];

            int baseEncoded = container>>>(2*containerAdress)&3;
            decodedBases[i] = new Base(baseEncoded);
        }
        return decodedBases;
    }

    public class Base implements Element.Base{
        private final int base;

        /**
         *
         * @param base: a 2 bit encoding of the nucleotide
         *            A-> 00
         *            G-> 01
         *            C-> 10
         *            T-> 11
         */
        // Change this for integers
        private Base(int base){
            this.base = base;
        }


        public Bin getBin() {
            return bin;
        }

        public long getWMin() {
            return 0;
        }

        public long getWMax() {
            return 0;
        }

        public long getRMin() {
            return 0;
        }

        public long getRMax() {
            return 0;
        }

        public long getSpan() {
            return 0;
        }

        public Base syndromize(int syndrome){
            int syndromized = base^syndrome;
            return new BaseFactory.Base(syndromized);
        }

        public Base getNucleotideAt(int index) {
            return this;
        }
    }
}
