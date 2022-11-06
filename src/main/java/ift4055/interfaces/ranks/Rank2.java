package ift4055.interfaces.ranks;

import ift4055.binning.Bin;
import ift4055.elements.dataElements.Base;
import ift4055.interfaces.Element;

public interface Rank2 extends Element{
    // Bin access
    public Bin getBin();
    public boolean isSameBin(Element E);

    // Parents and ancestors in the element tree.
    public Element getParent();
    public void setParent(Element E);
    public Element getContainer();
    public Element getRoot();


    // Children and descendants in the element tree
    public Element getChild(int index);
    public Element getChild();
    public Element[] getMembers();


    // Genome coordinates
    public int getWMin();
    public int getWMax();
    public int getRMin();
    public int getRMax();
    public int getLength();
    public int getSpan();


    // Strand calculations
    public int getStrand();
    public boolean isReverseStrand();
    public int getDiagonal();


    // DNA sequences
    public Base getNucleotideAt(int index);


    // Deletion
    public void delete();

}
