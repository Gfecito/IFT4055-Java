package ift4055.tests;

import java.io.File;
import java.io.IOException;

public class BinningSchemeTest {
    public static void run() throws IOException {
        System.out.println("Running data structure test for behaviour visualization");
        File readFile = new File("/Users/santi/Desktop/School/IFT4055/Testing/htsjdk-master/src/main/resources/data.bam");
        File writeFile = new File("/Users/santi/Desktop/School/IFT4055/Testing/htsjdk-master/src/main/resources/dataReWritten.sam");
        System.out.println("Test completed");
    }
}
