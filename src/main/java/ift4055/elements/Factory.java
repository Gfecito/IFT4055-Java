package ift4055.elements;

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
}
