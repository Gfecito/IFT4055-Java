package ift4055.storage;
import ift4055.elements.dataElements.Insert;
import ift4055.elements.dataElements.Base;
import ift4055.elements.dataElements.Segment;
import ift4055.elements.dataElements.Group;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class Assembly {
    private List<Chromosome> chromosomeList;

    public String[] split(Chromosome x){
        String sequence = x.getSequence();
        return sequence.split("N");
    }

    /*
    public Insert insert(String sequence){
        Insert insert = new Insert();
        char[] bases = sequence.toCharArray();
        for (char base: bases) {
            // For 4 different nucleotides
            // Starts at 00
            BitSet encoding = new BitSet(4);

            switch (Character.toLowerCase(base)){
                case('a'):
                    break;              // 00
                case('g'):
                    encoding.set(0);    // 01
                    break;
                case('c'):
                    encoding.set(1);    // 10
                    break;
                case('t'):
                    encoding.set(0);
                    encoding.set(1);    // 11
                    break;
                default:
                    throw new IllegalArgumentException("received invalid nucleotide");
            }
            new Base(encoding);
        }
        return insert;
    }
    */

    public Segment segment(String[] sequences){
        Segment segment = new Segment();
        for (String sequence : sequences) {
            //Insert insert = insert(sequence);
        }
        return segment;
    }

    public Group group(List<Chromosome> chromosomes){
        Group group = new Group();
        for (Chromosome chromosome : chromosomes) {
            Segment segment = segment(split(chromosome));
        }
        return group;
    }
}