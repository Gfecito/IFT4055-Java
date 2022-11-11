package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Factory;

public class BaseFactory implements Factory.Base {
    private int[] bases;   // Little endian encoding
    private int nBases;
    private Bin bin;

    public Base addBase(int b){
        int index = this.nBases;
        int B = 16;                     // 32 bits in int => 2 bits per base => 16 bases per int.
        int containerIndex =index/B;    // In which int?
        int containerAdress=index%B;    // Which bits in the int?
        int container = this.bases[containerIndex];
        container += (b>>>(2*containerAdress));       // Save new base in this integer.

        this.bases[containerIndex] = container;
        this.nBases++;
        return new Base(b);
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

        public int getWMin() {
            return 0;
        }

        public int getWMax() {
            return 0;
        }

        public int getRMin() {
            return 0;
        }

        public int getRMax() {
            return 0;
        }

        public Base getNucleotideAt(int index) {
            return this;
        }
    }
}
