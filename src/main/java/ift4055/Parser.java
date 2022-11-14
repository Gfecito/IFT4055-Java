package ift4055;

import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import ift4055.binning.Scheme;
import ift4055.binning.Bin;
import ift4055.elements.Element.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Parser {
    Scheme scheme;
    File sam;
    File fasta;

    public Parser(File sam, File fasta){
        this.sam = sam;
        this.fasta = fasta;

        this.scheme = new Scheme(2,10);
    }

    public Scheme populateBinningScheme() throws IOException {
        // There's obviously a problem here: schemes have 1 segment each, yet I place every segment in the same
        // scheme presently. Gotta fix. Not sure how. TODO.
        final Group referenceGenome = storeReferenceGenome(fasta, scheme);
        final Group pairedReads = storePairedReads(sam, scheme);

        return scheme;
    }

    private Group storeReferenceGenome(File fasta, Scheme scheme) {
        final FastaSequenceFile reader = new FastaSequenceFile(fasta, true);
        Bin root = new Bin(0, 0, scheme);
        LinkedList<Segment> groupChildren = new LinkedList<>();

        // For each chromosome/contig, make a segment.
        int readPointer = 0;
        ReferenceSequence chromosome = reader.nextSequence();
        while (chromosome != null) {
            Segment chainParent = root.newSegment();

            String name = chromosome.getName();
            String[] splitted = split(chromosome.toString());

            chainParent.setName(name);
            Segment[] chainSegments = new Segment[splitted.length];
            // For each non-ambiguous stretch in the contig, make an insert.
            for (int i = 0; i < splitted.length; i++) {
                String sequence = splitted[i];
                Segment segment = root.newSegment();

                byte[] bases = toBases(sequence);
                int length = sequence.length();
                int span = length - 1;
                int strand = 1; // I guess? Maybe not. TODO
                int rMin = readPointer;
                int wMin = readPointer;
                int offset = 0;
                Insert insert = root.newInsert(strand, rMin, span, wMin, bases, offset);

                readPointer += length;
                segment.combine(insert);
                chainSegments[i] = segment;
            }

            // Circular chain through parent links.
            for (int i = 1; i < chainSegments.length; i++)
                chainSegments[i].setParent(chainSegments[i - 1]);
            chainSegments[0].setParent(chainParent);                // Stops at direct child of group
            chainParent.setChild(chainSegments[0]);

            groupChildren.add(chainParent);
            scheme.addNamedElement(name, chainParent);
            chromosome = reader.nextSequence();
        }
        // Finished reading.
        reader.close();

        // Put all the children in group.
        Group group = root.newGroup(groupChildren.size());
        int i = 0;
        for (Segment s : groupChildren) {
            group.setChild(i, s);
            i++;
        }
        String name = fasta.getName();
        group.setName(name);
        scheme.addNamedElement(name, group);
        return group;
    }

    private Group storePairedReads(File sam, Scheme referenceScheme) throws IOException {
        final SamReader samReader = SamReaderFactory.makeDefault().open(sam);

        // From beginning to end.
        SAMRecordIterator iterator = samReader.queryOverlapping(sam.getName(), 0, 0);
        while (iterator.hasNext()) {
            SAMRecord alignment = iterator.next();
            // "All mapped segments in alignment lines are represented on the forward genomic strand"
            // -SAM format specification
            int strand = 1;

            /*
            Lookup the Segment element x associated with the reference contig or chromosome name (RNAME).
            (x should not be null.)
            Retrieve the binning scheme for x (instantiate if it does not exist yet).
            Select the bin B that contains the Rmin..Rmax interval of the alignment.
            Rmin = getAlignmentStart() − 1; Rmax = getAlignmentEnd() − 1.
            */
            String name = alignment.getReadName();
            Segment x = (Segment) referenceScheme.getNamedElement(name); // If scheme not found, instantiate.
            Scheme scheme = null;

            int Rmin = alignment.getAlignmentStart() - 1;
            int Rmax = alignment.getAlignmentEnd() - 1;
            Bin B = scheme.binByInterval(Rmin, Rmax);


            /*
            Lookup the Group element u associated with the template name (QNAME);
            if none (u = null), then initialize u ← B.newGroup(2) with null members and assign u.name.
            */
            name = sam.getName();
            Group u = (Group) referenceScheme.getNamedElement(name);
            if (u == null) {
                u = B.newGroup(2);
                u.setName(name);
            }



            /*
            Create a container y ← B.newSegment() and add its Match and Insert children z as y ← combine(y, z)
            by parsing the alignment in read position order.
            Match elements are created in the lowest bin that accommodates them;
            Insert elements are placed bin of the previous Match element
            or in B if the alignment starts with insertion.
            Soft-clipping and hard-clipping are not stored.

            Check the strand from FLAGS.
            Then process the CIGAR operations while keeping track of reading and writing positions.
             */
            Segment y = B.newSegment();
            // and add its Match and Insert children z as y ← combine(y, z) by parsing
            // the alignment in read position order
            int flags = alignment.getFlags();
            List<CigarElement> cigarElements = alignment.getCigar().getCigarElements();
            CigarOperator match = CigarOperator.M;
            CigarOperator insert = CigarOperator.I;

            int rMin;
            int span;
            int wMin;
            byte[] sequence = alignment.getReadBases();
            int offset = 0;
            Bin bin = B;

            // Store insert in B if starts with insertion
            if (cigarElements.get(0).getOperator().equals(insert)) {
                CigarElement e = cigarElements.remove(0);
                rMin = wMin = offset;
                span = e.getLength() - 1;
                offset += span + 1;

                bin.newInsert(strand, rMin, span, wMin, sequence, offset);
            }
            // Store the rest, only insert and matches for now.
            for (CigarElement e : cigarElements) {
                if (e.getOperator().equals(insert)) {
                    rMin = wMin = offset;
                    span = e.getLength() - 1;
                    offset += span + 1;

                    bin.newInsert(strand, rMin, span, wMin, sequence, offset);
                } else if (e.getOperator().equals(match)) {
                    rMin = wMin = offset;
                    span = e.getLength() - 1;
                    offset += span + 1;

                    bin = scheme.binByInterval(rMin, rMin + offset);

                    bin.newMatch(strand, rMin, span, wMin, sequence, offset);
                }
            }


            /*
            Retrieve read order from FLAGS, setting i ← 0 for first read, i ← 1 for second (last) read. Then combine:
             */
            int i = ((flags&64)==64)? 1: ((flags&128)==128)? 0: -1;     // -1 for neither first nor second.
            u = combine(u,i,y);
        }
        String name = sam.getName();
        samReader.close();


        Group u = (Group) referenceScheme.getNamedElement(name);
        return u;
    }

    private Group combine(Group u, int i, Segment y){
        Segment s = (Segment) u.getChild(i);
        if(s==null){
            u.setChild(y); y.setParent(u);
            Bin lca = Bin.lowestCommonAncestor(u.getBin(),y.getBin());
            if(lca!=u.getBin()) u = y.raiseGroup();
        }
        else{ s = s.combine(y); u = (Group) s.getParent();}

        return u;
    }

    private byte[] toBases(String sequence) {
        char[] nucleotides = sequence.toUpperCase().toCharArray();
        byte[] bases = new byte[nucleotides.length];
        for (int i = 0; i < nucleotides.length; i++) {
            char nucleotide = nucleotides[i];
            switch (nucleotide) {
                case 'A':
                    bases[i] = 0;
                    break;
                case 'G':
                    bases[i] = 1;
                    break;
                case 'C':
                    bases[i] = 2;
                    break;
                case 'T':
                    bases[i] = 3;
                    break;
            }
        }
        return bases;
    }

    private String[] split(String chromosome) {
        return chromosome.split("N");
    }
}