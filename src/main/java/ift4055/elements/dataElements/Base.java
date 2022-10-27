package ift4055.elements.dataElements;
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
    public Base(BitSet base){
        this.base = base;
    }
}
