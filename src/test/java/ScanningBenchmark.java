import io.github.utk003.json.Scanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScanningBenchmark {
    private static long time;
    private static void markStartTime() {
        time = System.nanoTime();
    }
    private static long readAndPrintTime(String message) {
        long delta = System.nanoTime() - time;
        if (message != null) {
            System.out.println(message + ": " + delta / 1_000_000.0 + " ms");
            System.out.println(message + ": " + delta / 1_000_000_000.0 + " s");
        }
        return delta;
    }

    public static void main(String[] args) throws IOException {
        String fileName = "test.json";
        scanningBenchmark(fileName);
    }

    private static void scanningBenchmark(String file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String s;
        char[] arr;
        markStartTime();
        while ((s = br.readLine()) != null)
            arr = s.toCharArray();
        readAndPrintTime("Raw BufferedReader Input");

        System.out.println();

        Scanner sc = new Scanner(new FileInputStream(file));
        markStartTime();
        while (sc.hasMore())
            sc.advance();
        long t = readAndPrintTime("Scanner-Tokenized Input");

        System.out.println();

        System.out.println("Number of tokens: " + sc.tokensPassed());
        System.out.println("Time per token: " + t / 1000.0 / sc.tokensPassed() + " μs (microseconds)");
    }
}
