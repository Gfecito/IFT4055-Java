package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;
import ift4055.interfaces.ranks.Rank2;
import ift4055.interfaces.ranks.Rank3;
import ift4055.interfaces.ranks.Rank4;


public class Segment extends Element{
    private interface SegmentChild extends Rank3, Rank2{}
    private interface SegmentParent  extends SegmentChild, Rank4{}

    private SegmentChild child;
    private SegmentParent parent;

}
