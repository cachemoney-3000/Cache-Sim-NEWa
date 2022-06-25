/*
Name: Joshua Samontanez
Course: EEL 4768 Summer 2022
Assignment title: Project 1 - Cache Simulator
*/
package Project1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Sim {

    public static void main(String[] args) {
        System.out.println("Please enter the arguments in the format of: <CACHE_SIZE> <ASSOC> <REPLACEMENT> <WB> <TRACE_FILE>");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] arguments = validateInputs(input);

        // Get the arguments from the user
        int cacheSize = Integer.parseInt(arguments[0]);
        int assoc = Integer.parseInt(arguments[1]);
        int blockSize = Integer.parseInt(arguments[2]); // BLOCK_SIZE is 64 by default
        int replacement = Integer.parseInt(arguments[3]);
        int WB = Integer.parseInt(arguments[4]);
        String traceFile = arguments[5];

        // Determine the offset bits, number of sets and the set bits
        int offsetBits = log2(blockSize);
        int numSets = cacheSize / (blockSize * assoc);
        int setBits = log2(numSets);

        if (replacement == 0) {
            Cache cache = new Cache(assoc, numSets);
            // Read the trace file
            readFile(traceFile, setBits, offsetBits, WB, replacement, cache, null);
        }
        else {
            CacheFIFO cache = new CacheFIFO(assoc, numSets);
            // Read the trace file
            readFile(traceFile, setBits, offsetBits, WB, replacement, null, cache);
        }
    }


    public static String[] validateInputs(String args) {
        String[] inputs = args.split(" ");

        String cacheSize = "";
        String assoc = "";
        String blockSize = "64"; // BLOCK_SIZE is 64 by default
        String replacement = "";
        String WB = "";
        String traceFile = "";

        String invalid = "Invalid input:";

        if (inputs.length != 5) {
            System.out.println("Invalid input:");
            System.out.print("A valid input must look like: <CACHE_SIZE> <ASSOC> <REPLACEMENT> <WB> <TRACE_FILE>");
        }
        else {
            // Getting the inputs
            try {
                cacheSize = inputs[0];
                assoc = inputs[1];
                replacement = inputs[2];
                WB = inputs[3];
                traceFile = inputs[4];
            } catch (NumberFormatException e) {
                System.out.println(invalid);
                System.out.print("Cache size must be an integer");
            }
        }

        return new String[] {cacheSize, assoc, blockSize, replacement, WB, traceFile};
    }


    public static void readFile (String file, int setBits, int offsetBits, int WB, int replacement, Cache cache, CacheFIFO cacheFIFO) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Read each line then split them
                String[] info = line.split(" ");
                String operationType = info[0];
                String hex = info[1];

                // Store the information such as set, tag, and offset to adr
                Address adr = new Address(hex, offsetBits, setBits);

                // LRU
                if (replacement == 0 && cache != null) {
                    LRU(WB, cache, adr,operationType);
                }
                else if (replacement == 1 && cacheFIFO != null){
                    FIFO(WB, cacheFIFO, adr, operationType);
                }

            }
        } catch (IOException e) {
            System.out.println("File not found");
        }

        printResults(replacement, cacheFIFO, cache);

    }

    // Replacement policy = LRU
    public static void LRU(int WB, Cache cache, Address adr, String operationType) {
        // Write-through
        if (WB == 0) {
            cache.writeThrough(adr.getSetDecimal(), adr.getTagHex(), operationType);
        }
        // Write-back
        else {
            // Read
            if (operationType.equals("R")) {
                cache.read(adr.getSetDecimal(), adr.getTagHex());
            }
            // Write
            else {
                cache.write(adr.getSetDecimal(), adr.getTagHex());
            }
        }
    }

    // Replacement policy = FIFO
    public static void FIFO(int WB, CacheFIFO cacheFIFO, Address adr, String operationType) {
        // Write-through
        if (WB == 0) {
            cacheFIFO.writeThrough(adr.getSetDecimal(), adr.getTagHex(), operationType);
        }
        // Write-back
        else {
            // Read
            if (operationType.equals("R")) {
                cacheFIFO.read(adr.getSetDecimal(), adr.getTagHex());
            }
            // Write
            else {
                cacheFIFO.write(adr.getSetDecimal(), adr.getTagHex());
            }
        }
    }

    public static void printResults(int replacementPolicy, CacheFIFO cacheFIFO, Cache cache) {
        float missRatio;
        float hitRatio;
        int total;
        int numMiss;
        int numHits;
        int numRead;
        int numWrite;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        // LRU
        if (replacementPolicy == 0) {
            total = cache.getNumHits() + cache.getNumMiss();
            missRatio = ((float) cache.getNumMiss()/total) * 100;
            hitRatio = ((float) cache.getNumHits()/total) * 100;

            numHits = cache.getNumHits();
            numRead = cache.getNumRead();
            numMiss = cache.getNumMiss();
            numWrite = cache.getNumWrite();
        }
        // FIFO
        else {
            total = cacheFIFO.getNumHits() + cacheFIFO.getNumMiss();
            missRatio = ((float) cacheFIFO.getNumMiss()/total) * 100;
            hitRatio = ((float) cacheFIFO.getNumHits()/total) * 100;

            numHits = cacheFIFO.getNumHits();
            numRead = cacheFIFO.getNumRead();
            numMiss = cacheFIFO.getNumMiss();
            numWrite = cacheFIFO.getNumWrite();
        }

        System.out.println("MISS = " + numMiss + " HITS = " + numHits);
        System.out.println("READ = " + numRead + " WRITE = " + numWrite);
        System.out.println("MISSRATIO = "  + df.format(missRatio) + "%");
        System.out.println("HITRATIO = " + df.format(hitRatio) + "%");
    }

    // Perform a log base 2 operation
    public static int log2(int n) {return (int)(Math.log(n) / Math.log(2));}
}
