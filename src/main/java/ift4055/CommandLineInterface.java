package ift4055;


import java.io.*;

public class CommandLineInterface {

    public static void main(String[] args) throws IOException {
        System.out.println("\n\n\n\nProgram started\n\n");
        System.out.println("To populate a binning structure based on a FASTA and SAM file please enter the command:");
        System.out.println("populate path/to/file.fasta path/to/file.sam");
        System.out.println("To create the assembly graph after populating binning scheme using GFA file please enter:");
        System.out.println("graph path/to/file.gfa");
        System.out.println("To export the binning structure please wait for me to implement that");
        System.out.println("To exit enter `quit`");

        try {
            InputStream inStream = System.in;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));

            String line;
            Parser parser = new Parser(null,null);
            while ((line = buffReader.readLine()) != null) {
                if (line.equalsIgnoreCase("quit")) break;
                String[] arguments = line.split(" ");
                if(arguments[0].equalsIgnoreCase("populate")){
                    try {
                        File fasta = new File(arguments[1]);
                        File sam = new File(arguments[2]);
                        parser = new Parser(sam, fasta);
                        parser.populateBinningScheme();
                    }
                    catch (Exception e){
                        System.out.println("Error while trying to populate scheme: "+e);
                    }
                }
                if(arguments[0].equalsIgnoreCase("graph")){
                    try {
                        File gfa = new File(arguments[1]);
                        parser.readAssembly(gfa);
                    }
                    catch (Exception e){
                        System.out.println("Error while trying to create assembly graph: "+e);
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
