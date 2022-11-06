package ift4055.interfaces.ranks;

import ift4055.binning.Bin;
import ift4055.interfaces.Element;

public interface Rank4 extends Element{
    // Bin access
    public Bin getBin();
    public boolean isSameBin(Element E);

    // Parents and ancestors in the element tree.
    public Element getParent();
    public Element getContainer();
    public Element getRoot();


    // Children and descendants in the element tree
    public Element getChild(int index);
    public Element getChild();
    public Element[] getMembers();
    public void setChild(int index, Element E);
    public void setChild(Element E);
    public int numChildren();



    // Deletion
    public void delete();
}
