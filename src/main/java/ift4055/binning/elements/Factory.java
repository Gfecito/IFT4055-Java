package ift4055.binning.elements;

import ift4055.assemblyGraph.Graph;

public interface Factory {
    interface Group{
        Element.Group newGroup(int nMembers);
    }
    interface Segment{
        Element.Segment newSegment();
    }
    interface Match{
        Element.Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset);
    }
    interface Syndrome{
        Element.Syndrome newSyndrome(int syndrome, int readPosition);
    }
    interface Insert{
        Element.Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset);
    }
    interface Base{
        Element.Base addBase(int b);
    }


    /* GRAPH */
    interface Edge{
        Graph.Edge newEdge(int i, int sigma);
    }
    interface Node{
        Graph.Node newNode();
    }
    interface Connector{
        Graph.Connector newConnector();
    }
}
