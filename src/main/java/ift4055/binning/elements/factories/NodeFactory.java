package ift4055.binning.elements.factories;

import ift4055.assemblyGraph.Graph;
import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.Bin;
import ift4055.binning.elements.Factory;

import java.util.LinkedList;


public class NodeFactory implements Factory.Node {
    private Node[] objects;
    private final Node sentinel;
    Bin bin;

    public NodeFactory(Bin b)
    {
        objects = new Node[16];
        this.bin = b;

        sentinel = new Node();
        sentinel.parent = sentinel.child = sentinel;
        for (int i = 1; i < objects.length; i++){
            Node s = new Node();
            s.parent = null;
            if(i==1) s.child = sentinel;              // End of circular list.
            else s.child = objects[i-1];              // Next in circular list.
            objects[i] = s;
        }
        sentinel.child = objects[objects.length-1];                       // Start of circular list.

        objects[0] = sentinel;
    }

    public Node newNode(){
        if (sentinel.child == sentinel){
            // if no empty then extend
            expandCapacity();
            Node s = new Node();
            s.parent = s.child = s;
            return s;
        }
        else {
            Node s = (Node) sentinel.child;
            sentinel.child = s.child;
            s.parent=s.child=s;
            return s;
        }
    }

    /**
     * Replace the current object array with a bigger one,
     * to be used whenever the previous one is filled.
     */
    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Node[] newObjects = new Node[c];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);
        for (int i = objects.length; i < c; i++){
            Node s = new Node();
            s.parent = null;
            if(i==objects.length) s.child = sentinel;     // End of circular list.
            else s.child = newObjects[i-1];               // Next in circular list.
            newObjects[i] = s;
        }
        sentinel.child = newObjects[c-1];                        // Start of circular list.

        this.objects = newObjects;
    }


    public Node[] getNodes(){
        LinkedList<Node> nodes = new LinkedList<>();
        for (Node node : objects) if(node.parent!=null) nodes.add(node);

        Node[] nodeArray = new Node[nodes.size()];
        nodeArray = nodes.toArray(nodeArray);

        return nodeArray;
    }


    private class Node implements Graph.Node {
        private GraphMember parent;
        private GraphMember child;



        public Bin getBin() {
            return bin;
        }

        public GraphMember getParent(){
            return parent;
        }

        public GraphMember getChild(){
            return child;
        }

        public void setParent(GraphMember E) {
            parent = E;
        }
        public void setChild(GraphMember E) {
            child = E;
        }


        public void delete() {
            parent = null;
            child = sentinel.child;
            sentinel.child = this;
        }

        public Graph.Node join(Graph.Node v) {
            Graph.Node u,w,v2,u2;
            u = this;
            if(v==null) return this;

            Bin B = Bin.lowestCommonAncestor(u.getBin(),v.getBin());
            if(!B.equals(v.getBin()) && B.equals(u.getBin())) return v.join(u);
            if(B.equals(v.getChild().getBin()) && v.getChild().getRank()==3){w = v; v2 = (Node) v.getChild();}
            else{
                w = B.newNode(); v2 = B.newNode();
                w.setChild(v2); v2.setParent(w); v2.setChild(v); v.setParent(v2);
            }
            if(B.equals(u.getBin())) u2 = u;
            else{u2 = B.newNode(); u2.setChild(u); u.setParent(u2);}
            u2.setParent(v2); w.setChild(u2);

            return w;
        }
    }
}
