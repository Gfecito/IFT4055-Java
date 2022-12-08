package ift4055;


import ift4055.binning.Scheme;
import ift4055.tests.BinningSchemeTest;

import java.io.*;

public class CommandLineInterface {

    public static void main(String[] args) throws IOException {
        System.out.println("\n\n\n\nProgram started\n\n");
        System.out.println("You can populate a binning structure based on a FASTA file.and SAM file.");
        System.out.println("You can add its paired reads through the group's corresponding SAM file.");
        System.out.println("You can also create the assembly graph after populating the binning scheme using GFA file.");
        System.out.println("To do so insert the following command, with the options (-fasta, -sam, -gfa) you wish.");
        System.out.println("-fasta path/to/reference.fasta -sam path/to/pairedReads.sam -gfa path/to/assembly.gfa");
        System.out.println("To visualize a test of the binning structure enter 'test'");
        System.out.println("To exit enter `quit`");

        try {
            File fasta, sam, gfa;
            boolean fastaRead, samRead, gfaRead;
            InputStream inStream = System.in;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));

            Scheme root = new Scheme(null, 0, 2, 10);
            fasta = null;   fastaRead = false;
            sam = null;     samRead = false;
            gfa = null;     gfaRead = false;
            String line;
            while ((line = buffReader.readLine()) != null) {
                if (line.equalsIgnoreCase("quit")) break;
                if (line.equalsIgnoreCase("test")){ BinningSchemeTest.run(); continue;}
                String[] arguments = line.split(" ");


                try {
                    boolean valid = false;
                    int i;
                    for (i = 0; i < arguments.length; i++) {
                        switch (arguments[i]) {
                            case "-fasta":
                                valid = true; i++;
                                fasta = new File(arguments[i]);
                                break;
                            case "-sam":
                                valid = true; i++;
                                sam = new File(arguments[i]);
                                break;
                            case "-gfa":
                                valid = true; i++;
                                gfa = new File(arguments[i]);
                            default:
                                if(!valid)  throw new RuntimeException("Invalid command");
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e + ": " + line);
                } catch (Exception e) {
                    System.out.println("Error while trying to read command: " + e);
                }

                if (fasta == null) System.out.println("No fasta file has been provided, but is necessary.");
                else try {
                    if(!fastaRead){
                        root.storeReferenceGenome(fasta);
                        fastaRead = true;
                    }
                } catch (Exception e) {
                    System.out.println("Error while trying to populate reference scheme: " + e);
                }
                if (sam == null) System.out.println("No sam file provided, paired reads wont be accessible.");
                else try {
                    if(!samRead){
                        root.storePairedReads(sam);
                        samRead = true;
                    }
                } catch (Exception e) {
                    System.out.println("Error while trying to add paired reads: " + e);
                }
                if (gfa == null) System.out.println("No gfa file provided, assembly wont be built.");
                else try {
                    if(!gfaRead){
                        root.readAssembly(gfa);
                        gfaRead = true;
                    }
                } catch (Exception e) {
                    System.out.println("Error while trying to create assembly graph: " + e);
                }

                System.out.println("Finalized processing command: " + line);
            }

        } catch (IOException ioe) {
            System.out.println("Exception while reading input " + ioe);
        }
        System.out.println("\n\nProgram finalized execution successfully\n\n\n\n");
    }
}
