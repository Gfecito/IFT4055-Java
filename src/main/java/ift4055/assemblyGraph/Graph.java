package ift4055.assemblyGraph;


import ift4055.binning.elements.Element.*;
import ift4055.binning.Scheme;
import ift4055.binning.Bin;

/**
 * Our representation of the assembly graph.
 */
public interface Graph {
    /**
     * Elements of our assembly graph.
     */
    interface GraphMember{
        /**
         *
         * @return the rank in the scheme's hierarchy of this element.
         */
        int getRank();

        /**
         *
         * @return the bin where this element is located in its scheme.
         */
        Bin getBin();

        /**
         * Deletes this element. Usually by setting its child and parent to null.
         */
        void delete();
    }
    interface Edge extends GraphMember{
        default int getRank(){
            return 2;
        }

        /**
         *
         * @return the reference segment of this edge.
         */
        Segment getReference();

        /**
         *
         * @return a tuple with the interval covered by this edge (start, end).
         */
        int[] getInterval();

        /**
         *
         * @param i determines which parent to return.
         * @return one of the parent node of this edge, if i=0 returns the source, if i=1 returns the target.
         */
        Node getParent(int i);

        /**
         *
         * @param i determines which parent to set; 0 for source, 1 for target.
         * @param v the node which will be set as that parent.
         */
        void setParent(int i, Node v);

        /**
         *
         * @param e the edge of which we wish to find the end node.
         * @param alpha the direction in which we search: source or target.
         * @return the end node in that direction.
         */
        static Node getEndNode(Edge e, int alpha){
            Node u = e.getParent(alpha);
            // Search for the node connected to a Connector
            while (u.getParent().getRank()!=4) u = (Node) u.getParent();
            if(u==null) throw new RuntimeException("Empty end node!");
            return u;
        }

        /**
         * Initializes an edge over a segment.
         * @param x the segment corresponding to our edge.
         * @return the new edge.
         */
        static Edge initEdge(Segment x){
            Scheme H = x.getScheme();
            int i = (int) x.getWMin(); int sigma = (int) (1 + x.getWMax() - i);
            Bin B = H.coveringBin(i,(i+sigma-1));
            Edge e = B.newEdge(i, sigma);
            Node u = B.newNode(); u.setChild(e); e.setParent(0,u);
            Node v = B.newNode(); v.setChild(e); e.setParent(1,v);
            Connector s, t;
            Bin C = x.getBin(); s = C.newConnector(); t = C.newConnector();
            Node u2 = B.newNode(); s.setChild(0,u2); s.setChild(1,u); u2.setParent(s); u.setParent(s);
            Node v2 = B.newNode(); t.setChild(0,v); t.setChild(1,v2); v2.setParent(s); v.setParent(s);
            return e;
        }

        /* INSTANCE METHODS */

        /**
         * Connects this edge with another one.
         * @param alpha is this edge the target or the source.
         * @param b the other edge we connect to.
         * @param beta is b the target or the source.
         * @return the connector connecting this edge to b.
         */
        default Connector attach(int alpha, Edge b, int beta){
            Edge a = this;
            Node u,v;
            Connector s,t,r;
            u = getEndNode(a,alpha); v = getEndNode(b, beta);
            s = (Connector) u.getParent(); t = (Connector) v.getParent();
            if(s.getChild(0)==u) {   // Source?
                if (t.getChild(0) == v) r = s.join(t, -1);
                else r = s.join(t, 1);
            }
            else{   // Target
                if (t.getChild(0) == v) r = s.join(t, 1);
                else r = s.join(t, -1);
            }
            return r;
        }

        /**
         * Detaches a pair of endpoints. The detachment corresponds to uncoupling
         * the edges from their current endnodes, and creates a new connector edge.
         *
         * @param alpha determines if we take the source or target of this edge.
         * @param b the edge with the other endpoint.
         * @param beta determines if we take the source or target of b.
         * @return the new connector edge, adjacent to a and b.
         */
        default Connector detach(int alpha, Edge b, int beta){
            Edge a = this;
            Node u,v;
            Connector s, vp, up;
            Bin A,B,C;
            u = getEndNode(a,alpha); v = getEndNode(b, beta);
            u.deleteMember(a.getParent(alpha)); v.deleteMember(b.getParent(beta));
            if(!v.getParent().equals(u.getParent())){vp = (Connector) v.getParent(); vp.deleteIfDisjoint();}
            up = (Connector) u.getParent(); up.deleteIfDisjoint();
            A = a.getBin();
            B = b.getBin();
            u = A.newNode();
            if(a.equals(b) && alpha==beta) v=u; else v = B.newNode();
            u.setChild(a); a.setParent(alpha, u); v.setChild(b); b.setParent(beta, v);
            C = Bin.lowestCommonAncestor(A,B);
            s = C.newConnector();
            s.setChild(0,u); u.setParent(s); s.setChild(1, v); v.setParent(s);
            return s;
        }
    }

    /**
     * Elements maintaining the hierarchy and structure of our graph.
     */
    interface Node extends GraphMember{
        default int getRank(){
            return 3;
        }

        /**
         *
         * @return the parent of this node.
         */
        GraphMember getParent();

        /**
         *
         * @return the child of this node.
         */
        GraphMember getChild();

        /**
         *
         * @param parent the new parent of this node.
         */
        void setParent(GraphMember parent);

        /**
         *
         * @param child the new child of this node.
         */
        void setChild(GraphMember child);



        /* INSTANCE METHODS */

        /**
         * Joins this node and v, creating a new node, connected with this and v.
         * @param v the node we join to.
         * @return the new node.
         */
        Node join(Node v);

        /**
         * Removes a leaf v from u, succeeds if v is connected to v, fails otherwise.
         * @param v the leaf
         * @return whether we succeeded in removing the leaf v from u
         */
        default boolean deleteMember(Node v){
            GraphMember w0,v2;
            Node u,w,w2,v3;
            u = this;
            if(u.equals(v)){u.setChild(u); return true;}
            w0 = u.getChild(); if(w0.equals(u)) return false;
            if(w0.getRank()==2) return false;
            // Not rank 2? Rank 3.
            w = (Node) w0;
            if(w.equals(v)) {u.setChild(w.getParent()); w.delete(); return true;}
            v2 = w.getChild();
            if(v2.getRank()==3){
                v3 = (Node) v2; // v3 = v2 casted as a Node.
                if(v3.deleteMember(v)){ // Success in head's sublist
                    if(v3.getChild().equals(v3)){u.setChild(w.getParent()); w.delete(); v3.delete();}
                    return true;
                }
            }

            while(!w.getParent().equals(u)){
                w2 = (Node) w.getParent(); w = (Node) w.getChild(); v2 = w.getChild();
                if(w.equals(v)){
                    w2.setParent(w.getParent()); w.delete();
                    return true;
                }
                if(v2.getRank()==3){
                    v3 = (Node) v2; // v3 = v2 casted as a Node.
                    if(v3.deleteMember(v)){ // Success in head's sublist
                        if(v3.getChild().equals(v3)){ w2.setParent(w.getParent()); v3.delete(); }
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Ensure proper double-strandedness.
     */
    interface Connector extends GraphMember{
        default int getRank(){
            return 4;
        }

        /**
         *
         * @param i 0 to get the source, and 1 to get the target.
         * @return the source or target.
         */
        Node getChild(int i);

        /**
         *
         * @param i 0 to set the source, 1 to set the target.
         * @param node the node to be set.
         */
        void setChild(int i, Node node);

        /* INSTANCE METHODS */

        /**
         * Joins this connector and t, creating a new connector.
         * Give a warning if creates a loop connector (s = t and eta = −1).
         * @param t the connector we're connecting to.
         * @param eta the orientation of t.
         * @return the new connector, located at the lca.
         */
        Connector join(Connector t, int eta);

        /**
         * Deletes this connector if it is isolated/disjoint.
         * @return whether the deletion was successful.
         */
        default boolean deleteIfDisjoint(){
            Connector r = this;
            Node u,v;
            u = r.getChild(0); v = r.getChild(1);
            if(u.getChild().equals(u) && v.getChild().equals(v)){
                u.delete(); v.delete(); r.delete();
                return true;
            }
            return false;
        }
    }
}
