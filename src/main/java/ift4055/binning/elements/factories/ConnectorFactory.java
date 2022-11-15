package ift4055.binning.elements.factories;

import ift4055.assemblyGraph.Graph;
import ift4055.assemblyGraph.Graph.*;
import ift4055.binning.Bin;
import ift4055.binning.elements.Element;
import ift4055.binning.elements.Element.*;
import ift4055.binning.elements.Factory;


public class ConnectorFactory implements Factory.Connector {
    private Connector[] objects;
    private Connector sentinel;
    Bin bin;

    public ConnectorFactory(Bin b)
    {
        objects = new Connector[16];
        this.bin = b;

        sentinel = new Connector();
        sentinel.s = sentinel.t = sentinel;
        for (int i = 1; i < objects.length; i++){
            Connector s = new Connector();
            s.s = null;
            if(i==1) s.t = sentinel;              // End of circular list.
            else s.t = objects[i-1];              // Next in circular list.
            objects[i] = s;
        }
        sentinel.t = objects[objects.length-1];                       // Start of circular list.

        objects[0] = sentinel;
    }

    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Connector[] newObjects = new Connector[c];
        for (int i = 0; i < objects.length; i++)
            newObjects[i] = objects[i];
        for (int i = objects.length; i < c; i++){
            Connector s = new Connector();
            s.s = null;
            if(i==objects.length) s.t = sentinel;     // End of circular list.
            else s.t = newObjects[i-1];               // Next in circular list.
            newObjects[i] = s;
        }
        sentinel.t = newObjects[-1];                        // Start of circular list.

        this.objects = newObjects;
    }

    public Connector newConnector()
    {
        if (sentinel.t == sentinel)
        {
            // if no empty then extend
            expandCapacity();
            Connector s = new Connector();
            s.s = s.t = s;
            return s;
        } else
        {
            Connector s = (Connector) sentinel.t;
            sentinel.t = s.t;
            s.s=s.t=s;
            return s;
        }
    }


    private class Connector implements Graph.Connector {
        private GraphMember s;
        private GraphMember t;
        private String name;
        private Segment reference;
        private int orientation;


        public Bin getBin() {
            return bin;
        }

        public Node getChild(int index){
            if(index>1 || index<0) throw new IllegalArgumentException();
            return (Node) (index==1? t: s);
        }

        public void setChild(int index, Node child){
            if(index>1 || index<0) throw new IllegalArgumentException();
            if(index==1) t=child;
            else s=child;
        }

        public Graph.Connector join(Graph.Connector t, int eta) {
            if(this.equals(t) && orientation == 1) return this;
            Node u0, u1, v0, v1, w0, w1;
            Graph.Connector s,r;
            s = this;
            u0 = s.getChild(0); u1 = s.getChild(1);
            v0 = t.getChild((1-eta)/2);
            v1 = t.getChild((1+eta)/2);
            w0 = u0.join(v0);
            if(s.equals(t)) w1=w0;
            else w1 = u1.join(v1);
            Bin B = Bin.lowestCommonAncestor(s.getBin(), t.getBin());
            if(B.equals(s.getBin())) r = s;
            else{
                if(B.equals(t.getBin())) r = t;
                else r = B.newConnector();
            }
            r.setChild(0, w0); w0.setParent(r); r.setChild(1, w1); w1.setParent(r);
            if(!r.equals(s)) s.delete();
            if(!r.equals(t)) t.delete();

            return r;
        }

        public long getRMin() {
            return reference.getRMin();
        }

        public long getSpan() {
            return reference.getSpan();
        }

        public void delete() {
            s = null;
            GraphMember temp = sentinel.t;
            sentinel.t = this.t;
            this.t = temp;
        }

    }
}
