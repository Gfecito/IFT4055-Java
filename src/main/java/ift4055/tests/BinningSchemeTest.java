package ift4055.tests;

import ift4055.binning.Scheme;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BinningSchemeTest {
    /**
     * A test with toy .fasta, .sam, and .gfa files for a small binning scheme.
     * @throws IOException if there is trouble reading the toy files.
     * @throws InterruptedException if there is trouble upon pausing the thread (to avoid being overwhelming with logs).
     */
    public static void run() throws IOException, InterruptedException {
        System.out.println("Running data structure test for behaviour visualization");
        Scheme root = new Scheme(null, 0, 2, 4);

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString()+"/src/main/java/ift4055/tests";
        System.out.println("Current absolute path is: " + s);
        File fasta = new File(s+"/test.fasta");
        File sam = new File(s+"/test.sam");
        File gfa = new File(s+"/test.gfa");

        Thread.sleep(1000);
        root.storeReferenceGenome(fasta,false);
        root.storePairedReads(sam,false);
        Thread.sleep(1000);
        System.out.println(root);
        root.readAssembly(gfa,false);
        Thread.sleep(1000);


        System.out.println("Test completed");
    }
}
