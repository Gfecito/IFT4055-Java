package ift4055.interfaces;

import ift4055.binning.Bin;

public interface Element {
    public boolean isSameBin(Element E);
    public Bin getBin();
}
