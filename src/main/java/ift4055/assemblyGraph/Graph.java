package ift4055.assemblyGraph;


import ift4055.binning.elements.Element.*;
import ift4055.binning.Scheme;
import ift4055.binning.Bin;

public interface Graph {
    interface GraphMember{
        int getRank();
        Bin getBin();
    }
    interface Edge extends GraphMember{
        default int getRank(){
            return 2;
        }
        Segment getReference();
        int[] getInterval();
        Node getParent(int i);

        void setParent(int i, Node v);
        void delete();



        static Node getEndNode(Edge e, int alpha){
            Node u = e.getParent(alpha);
            while (u.getParent().getRank()!=4) u = (Node) u.getParent();

            return u;
        }
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
    interface Node extends GraphMember{
        default int getRank(){
            return 3;
        }
        GraphMember getParent();
        GraphMember getChild();
        void setParent(GraphMember parent);
        void setChild(GraphMember child);


        void delete();


        /* INSTANCE METHODS */
        Node join(Node v);
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
        Edge initEdge();
    }
    interface Connector extends GraphMember{
        default int getRank(){
            return 4;
        }
        Node getChild(int i);

        void setChild(int i, Node node);
        void delete();

        /* INSTANCE METHODS */
        // Give a warning if creates a loop connector (s = t and eta = −1)
        Connector join(Connector t, int eta);
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
