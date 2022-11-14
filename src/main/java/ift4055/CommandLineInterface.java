package ift4055;

import htsjdk.samtools.*;
import htsjdk.samtools.seekablestream.SeekableStream;

import java.io.*;

public class CommandLineInterface {

    public static void main(String[] args) throws IOException {
        System.out.println("\n\n\n\nProgram started\n\n");
        System.out.println("To populate a binning structure based on a FASTA and SAM file please enter the command:");
        System.out.println("scan path/to/file.fasta path/to/file.sam");
        System.out.println("To export the binning structure please wait for me to implement that");
        System.out.println("To exit enter `quit`");

        try {
            InputStream inStream = System.in;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));

            String line;
            while ((line = buffReader.readLine()) != null) {
                if (line.equalsIgnoreCase("quit")) break;
                String[] arguments = line.split(" ");
                if(arguments[0].equalsIgnoreCase("scan")){
                    try {
                        File fasta = new File(arguments[1]);
                        File sam = new File(arguments[2]);
                        Parser parser = new Parser(sam, fasta);
                        parser.populateBinningScheme();
                    }
                    catch (Exception e){
                        System.out.println("Error while trying to read file: "+e);
                    }
                }
                else{System.out.println("Invalid command");}
            }
        } catch (IOException ioe) {
            System.out.println("Exception while reading input " + ioe);
        }
        System.out.println("\n\nProgram finalized execution successfully\n\n\n\n");
    }
}
