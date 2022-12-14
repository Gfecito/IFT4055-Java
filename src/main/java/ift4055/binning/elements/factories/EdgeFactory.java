package ift4055.binning.elements.factories;

import ift4055.assemblyGraph.Graph;
import ift4055.binning.Bin;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Factory;

public class EdgeFactory implements Factory.Edge {
    private Edge[] objects;
    private int index;
    Bin bin;

    public EdgeFactory(Bin bin){
        objects = new Edge[16];
        index = 0;
        this.bin = bin;
    }

    public Edge newEdge(int i, int sigma) {
        Edge insert = new Edge(i, sigma);

        if(index >= objects.length) expandCapacity();
        objects[index] = insert;
        index++;
        return insert;
    }

    /**
     * Replace the current object array with a bigger one,
     * to be used whenever the previous one is filled.
     */
    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Edge[] newObjects = new Edge[c];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);

        this.objects = newObjects;
    }

    public Edge[] getEdges(){
        Edge[] edges = new Edge[index];
        System.arraycopy(objects, 0, edges, 0, index);
        return edges;
    }

    private class Edge implements Graph.Edge {
        private Graph.Node s;    // encodes for 00 (-1) or 11 (1)
        private Graph.Node t;
        private Element.Segment reference;
        private final int start;
        private final int span;
        private Edge(int i, int sigma){
            start = i;
            span = sigma;
        }

        public Bin getBin(){
            return bin;
        }


        public Element.Segment getReference() {
            return reference;
        }

        public int[] getInterval() {
            return new int[]{start, start+span};
        }

        public Graph.Node getParent(int index){
            if(index>1 || index<0) throw new IllegalArgumentException();
            return index==1? t: s;
        }

        public void setParent(int index, Graph.Node child){
            if(index>1 || index<0) throw new IllegalArgumentException();
            if(index==1) t=child;
            else s=child;
        }

        public void delete(){s=t=null;}
    }
}
