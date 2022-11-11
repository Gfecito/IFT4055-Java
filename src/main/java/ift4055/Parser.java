package ift4055;

import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import ift4055.binning.Scheme;
import ift4055.binning.Bin;
import ift4055.elements.Element.*;

import java.io.File;
import java.io.IOException;

public class Parser {
    //     SAMRecord.java
    //     public byte[] getReadBases() {
    //        return mReadBases;
    //    }
    Scheme scheme;

    public Scheme populateBinningScheme(File alignementMap, File fasta, int start, int stop) throws IOException {
        final SamReader samReader = SamReaderFactory.makeDefault().open(alignementMap);
        final SamReader mateReader = SamReaderFactory.makeDefault().open(alignementMap);

        final FastaSequenceFile fastaReader = new FastaSequenceFile(fasta, true);
        final ReferenceSequence referenceSequence = fastaReader.nextSequence();

        final Scheme scheme = new Scheme();

        SAMRecordIterator it = samReader.queryOverlapping(alignementMap.getName(), start, stop);
        while (it.hasNext()) {
                SAMRecord r = it.next();
                SAMRecord mate = mateReader.queryMate(r);

                //do stuff
        }


        samReader.close();
        fastaReader.close();
        this.scheme = scheme;
        return scheme;
    }

    public Scheme storeChromosomes(Scheme scheme, FastaSequenceFile reader, String name){
        Group genome = null;
        genome.setName(name);
        Bin root = new Bin(-1,-1, scheme);
        // For each element in root bin

        Segment c = root.segmentFactory.newSegment();
        Insert x = root.insertFactory.newInsert(-1,-1,-1,-1,null,-1);
        c = Scheme.combine(c, x);
        return scheme;
    }

    public void readSamRecord(SAMRecord record){
        String name = record.getReadName();
    }


    private String[] split(String chromosome){
        return chromosome.split("N");
    }
}