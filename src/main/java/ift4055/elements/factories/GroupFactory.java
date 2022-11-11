package ift4055.elements.factories;

import ift4055.binning.Bin;
import ift4055.elements.Element;
import ift4055.elements.Factory;

import java.util.ArrayList;
import java.util.List;

public class GroupFactory implements Factory.Group {
    List<Group> objects;
    Bin bin;

    public GroupFactory(Bin bin){
        objects = new ArrayList<>();
        this.bin = bin;
    }
    public Group newGroup(int nMembers){
        Group group = new Group(nMembers);
        objects.add(group);
        return group;
    }
    public class Group implements Element.Group{
        Segment[] members;
        String name;
        private Group(int nMembers){
            members = new Segment[nMembers];
        }

        public Bin getBin(){
            return bin;
        }
        public Element getChild(int index){
            return members[index];
        }

        // Genome coordinates
        // Iterate through segment children and get mins and maxes.
        public int getWMin(){
            throw new UnsupportedOperationException();
        }
        public int getWMax(){
            throw new UnsupportedOperationException();
        }
        public int getRMin(){
            throw new UnsupportedOperationException();
        }
        public int getRMax(){
            throw new UnsupportedOperationException();
        }


        public Base getNucleotideAt(int index) {
            return null;
        }

        public void setParent(Element E) {
            throw new UnsupportedOperationException();
        }

        // Children and descendants in the element tree
        public Element[] getMembers(){
            return members;
        }
        public void setChild(int index, Element E){
            members[index] = (Segment) E;
        }

        // Deletion
        public void delete(){members=null;}

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
