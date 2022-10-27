package ift4055.elements.dataElements;
import ift4055.elements.Element;
import ift4055.interfaces.ElementMethods;

import java.util.HashSet;

public class Group extends Element {
    private int numberOfChildren;
    private HashSet<Segment[]> children;    // fixed-length arrays of rank-3 Segment children

    // By default
    public Group(){
        this.numberOfChildren = 2;
    }
    // Customized
    public Group(int numberOfChildren){
        this.numberOfChildren = numberOfChildren;
    }
}
