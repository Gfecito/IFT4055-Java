package ift4055.interfaces;

import htsjdk.samtools.Bin;

public interface ElementMethods {
    public Bin getBin();
    public boolean isSameBin(Bin E);

}
