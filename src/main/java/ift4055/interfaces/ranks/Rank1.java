package ift4055.interfaces.ranks;

import ift4055.binning.Bin;
import ift4055.elements.dataElements.Base;
import ift4055.interfaces.Element;

public interface Rank1 extends Element {


    // Parents and ancestors in the element tree.
    public Element getParent();
    public Element getContainer();
    public Element getRoot();


    // Genome coordinates
    public int getWMin();
    public int getWMax();
    public int getRMin();
    public int getRMax();



    // DNA sequences
    public Base getNucleotideAt(int index);

}
