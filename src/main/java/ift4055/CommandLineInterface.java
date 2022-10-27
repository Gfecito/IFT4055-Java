package ift4055;

import htsjdk.samtools.*;
import htsjdk.samtools.seekablestream.SeekableStream;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CommandLineInterface {

    public static void main(String[] args) throws IOException {
        System.out.println("Program started");
        File readFile = new File("/Users/santi/Desktop/School/IFT4055/IFT4055-Java/src/main/resources/data.bam");
        File writeFile = new File("/Users/santi/Desktop/School/IFT4055/IFT4055-Java/src/main/resources/dataReWritten.sam");
        convertReadNamesToUpperCase(readFile,writeFile);
        System.out.println("Program finalized execution successfully");
    }

    public static SeekableStream myIndexSeekableStream() {
        throw new UnsupportedOperationException();
    }

    /** Example usages of {@link htsjdk.samtools.SamReaderFactory} */
    public void openSamExamples() throws MalformedURLException {
        /**
         * Simplest case
         */

        /**
         * With different reader options
         */
        final SamReader readerFromConfiguredFactory =
                SamReaderFactory.make()
                        .enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX)
                        .validationStringency(ValidationStringency.SILENT)
                        .samRecordFactory(DefaultSAMRecordFactory.getInstance())
                        .open(new File("/my.bam"));

        /**
         * With a more complicated source
         */
        final SamReader complicatedReader =
                SamReaderFactory.makeDefault()
                        .open(
                                SamInputResource.of(new URL("http://broadinstitute.org/my.bam")).index(myIndexSeekableStream())
                        );

        /**
         * Broken down
         */
        final SamReaderFactory factory =
                SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.LENIENT);

        final SamInputResource resource = SamInputResource.of(new File("/my.bam")).index(new URL("http://broadinstitute.org/my.bam.bai"));

        final SamReader myReader = factory.open(resource);

        for (final SAMRecord samRecord : myReader) {
            System.err.print(samRecord);
        }

    }

    /**
     * Read a SAM or BAM file, convert each read name to upper case, and write a new
     * SAM or BAM file.
     */
    public static void convertReadNamesToUpperCase(final File inputSamOrBamFile, final File outputSamOrBamFile) throws IOException {

        final SamReader reader = SamReaderFactory.makeDefault().open(inputSamOrBamFile);

        // makeSAMorBAMWriter() writes a file in SAM text or BAM binary format depending
        // on the file extension, which must be either .sam or .bam.

        // Since the SAMRecords will be written in the same order as they appear in the input file,
        // and the output file is specified as having the same sort order (as specified in
        // SAMFileHeader.getSortOrder(), presorted == true.  This is much more efficient than
        // presorted == false, if coordinate or queryname sorting is specified, because the SAMRecords
        // can be written to the output file directly rather than being written to a temporary file
        // and sorted after all records have been sent to outputSam.

        final SAMFileWriter outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(),
                true, outputSamOrBamFile);

        for (final SAMRecord samRecord : reader) {
            // Convert read name to upper case.
            samRecord.setReadName(samRecord.getReadName().toUpperCase());
            outputSam.addAlignment(samRecord);
        }

        outputSam.close();
        reader.close();
    }
}
