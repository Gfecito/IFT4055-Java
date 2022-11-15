package ift4055.binning.elements.factories;

import ift4055.assemblyGraph.Graph;
import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.Bin;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.*;
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

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Edge[] newObjects = new Edge[c];
        for (int i = 0; i < objects.length; i++)
            newObjects[i] = objects[i];

        this.objects = newObjects;
    }

    public Edge newEdge(int i, int sigma) {
        Edge insert = new Edge(i, sigma);

        if(index >= objects.length) expandCapacity();
        objects[index] = insert;
        index++;
        return insert;
    }

    private class Edge implements Graph.Edge {
        private Node s;    // encodes for 00 (-1) or 11 (1)
        private Node t;
        private Segment reference;
        private int start;
        private int span;
        private Edge(int i, int sigma){
            start = i;
            span = sigma;
        }

        public Bin getBin(){
            return bin;
        }


        public Segment getReference() {
            return reference;
        }

        public int[] getInterval() {
            return new int[0];
        }

        public Node getParent(int index){
            if(index>1 || index<0) throw new IllegalArgumentException();
            return index==1? t: s;
        }

        public void setParent(int index, Node child){
            if(index>1 || index<0) throw new IllegalArgumentException();
            if(index==1) t=child;
            else s=child;
        }


        // Deletion
        public void delete(){s=t=null;}
    }
}
