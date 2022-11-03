package ift4055.interfaces;

import ift4055.binning.Bin;

public interface Element {
    // Bin access
    public Bin getBin();
    public boolean isSameBin(Element E);
}
