package ift4055.elements.dataElements;

import ift4055.interfaces.ranks.Rank1;

// Storage class for nucleotides
public final class Base implements Rank1 {
    private int base;

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


    // Deletion is not supported
    public static class Factory{
        private int[] bases;   // Little endian encoding
        private int nBases;

        public Factory(int totalBases){
            this.bases = new int[totalBases/16+1];
        }
        public int addBase(int b){
            int index = this.nBases;
            int B = 16;                     // 32 bits in int => 2 bits per base => 16 bases per int.
            int containerIndex =index/B;    // In which int?
            int containerAdress=index%B;    // Which bits in the int?
            int container = this.bases[containerIndex];
            container += (b>>>(2*containerAdress));       // Save new base in this integer.

            this.bases[containerIndex] = container;
            this.nBases++;
            return index;
        }
    }
}
