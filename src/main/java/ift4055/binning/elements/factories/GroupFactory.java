package ift4055.binning.elements.factories;

import ift4055.binning.Bin;
import ift4055.binning.elements.Factory;
import ift4055.binning.elements.Element;


public class GroupFactory implements Factory.Group {
    private Group[] objects;
    private int index;
    Bin bin;

    public GroupFactory(Bin bin){
        objects = new Group[16];
        index = 0;
        this.bin = bin;
    }

    public Group newGroup(int nMembers){
        Group group = new Group(nMembers);

        if(index >= objects.length) expandCapacity();
        objects[index] = group;
        index++;
        return group;
    }

    /**
     * Replace the current object array with a bigger one,
     * to be used whenever the previous one is filled.
     */
    private void expandCapacity(){
        int c = objects.length;
        c = c%3==0? 3*c/2: 4*c/3;
        Group[] newObjects = new Group[c];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);

        this.objects = newObjects;
    }

    public class Group implements Element.Group{
        Segment[] members;
        String name;
        private Group(int nMembers){
            members = new Segment[nMembers];
            for(int i=0; i<nMembers; i++)
                members[i]=null;
        }

        public Bin getBin(){
            return bin;
        }
        public Element getChild(int index){
            return members[index];
        }

        // Genome coordinates
        // Iterate through segment children and get mins and maxes.
        public long getWMin(){
            long min = members[0].getWMin();
            for (Segment member :
                    members) {
                long val = member.getWMin();
                if(val<min) min = val;
            }
            return min;
        }
        public long getWMax(){
            long max = members[0].getWMax();
            for (Segment member :
                    members) {
                long val = member.getWMax();
                if(val<max) max = val;
            }
            return max;
        }
        public long getRMin(){
            long min = members[0].getRMin();
            for (Segment member :
                    members) {
                long val = member.getRMin();
                if(val<min) min = val;
            }
            return min;
        }
        public long getRMax(){
            long max = members[0].getRMax();
            for (Segment member :
                    members) {
                long val = member.getRMax();
                if(val<max) max = val;
            }
            return max;
        }

        public long getSpan() {
            long sum = members[0].getSpan();
            for (Segment member :
                    members) {
                sum += member.getSpan();
            }
            return sum;
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
