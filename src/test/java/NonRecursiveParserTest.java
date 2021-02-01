import io.github.utk003.json.JSONParser;
import io.github.utk003.json.elements.JSONValue;

import java.io.*;

public class NonRecursiveParserTest {
    private static final String IN = "test/in/", OUT = "test/out/non-recursive/";
    public static void main(String[] args) throws IOException {
        String fileName = "test.json";

        System.out.println("Starting Non-Recursive JSON Parser Test on \"" + fileName + "\"");

        int numIterations = 1000;
        long start = System.nanoTime();
        for (int i = 0; i < numIterations; i++) {
            JSONValue json = JSONParser.parseNonRecursive(new FileInputStream(IN + fileName));
            json.println(new PrintStream(new FileOutputStream(OUT + fileName)));
        }
        long end = System.nanoTime();

        System.out.println("Parsing Duration: " + (end - start) / 1_000_000.0 / numIterations + " ms");
        System.out.println("Parsing Duration: " + (end - start) / 1_000_000_000.0 / numIterations + " s");

        System.out.println("JSON parsing output in \"" + OUT + fileName + "\"");
    }
}
