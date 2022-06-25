/*
Name: Joshua Samontanez
Course: EEL 4768 Summer 2022
Assignment title: Project 1 - Cache Simulator
*/
package Project1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheFIFO {
    private final int assoc;

    private int numHits = 0;
    private int numMiss = 0;
    private int numRead = 0;
    private int numWrite = 0;

    private HashMap<Integer, List<Integer>> FIFO;

    private final HashMap<Integer, HashMap<Integer, Block>> cache;

    public CacheFIFO(int assoc, int numSets) {
        this.assoc = assoc;

        //Cache construction
        cache = new HashMap<>();
        for (int i = 0; i < assoc; i++) {
            // Create each sets
            HashMap<Integer, Block> set = new HashMap<>();

            // Create a hashmap that keep tracks of the indices
            FIFO = new HashMap<>();
            for(int j = 0; j < numSets; j++) {
                // Initialize the list
                List<Integer> listFIFO = new ArrayList<>();
                listFIFO = initializeList(listFIFO);
                FIFO.put(j, listFIFO);
                // Populate the blocks for each sets
                Block block = new Block(0, 0, null);
                set.put(j, block);
            }
            // Populate each associativity
            cache.put(i, set);
        }
    }

    private List<Integer> initializeList(List<Integer> listFIFO) {
        for (int i = 0; i < assoc; i++) {
            listFIFO.add(i);
        }
        return listFIFO;
    }

    private boolean isHit(int setDecimal, String tag, boolean isRead, boolean isWriteThru) {
        // Loop through all the sets in a cache to find a hit
        for (int i = 0; i < assoc; i++){
            // Get the block info
            Block block = cache.get(i).get(setDecimal);
            String tagStored = block.getTag();
            int valid = block.getValid();

            // If a hit is found
            if (valid == 1 && tagStored.equals(tag)) {
                // Get the instance of the list
                List<Integer> listFIFO = FIFO.get(setDecimal);

                //System.out.println();
                //System.out.println("BEFORE = " + listFIFO);

                // Update the list by removing the first element then putting it to the back
                Integer x = i;
                listFIFO.remove(x);
                listFIFO.add(x);
                // Then update the hashmap
                FIFO.put(setDecimal, listFIFO);

                // Only applies if it's a write-back
                if(!isWriteThru) {
                    if(isRead)
                        block.setDirty(0);  // If the operation is read, set dirty to 0
                    else
                        block.setDirty(1); // If it's write, set dirty to 1
                }

                block.setValid(1); // Then set it to valid

                /*
                // ~ D E B U G G I N G ~
                System.out.println("AFTER = " + listFIFO);
                System.out.println("Hit at way " + i);
                System.out.println("(HIT) -> Index = " + setDecimal + " Tag = " + tag + " TagStored = " + tagStored);
                // ~ D E B U G G I N G ~

                 */

                // Increase the hits and the memory reads
                numHits++;
                numRead++;
                return true;
            }
        }
        return false;
    }

    private void miss (int setDecimal, String tag, boolean isRead) {
        numMiss++;  // Increment the miss tracker
        int wayNumber = findFIFO(setDecimal);   // Locate a block to use

        // Get the block information
        Block block = cache.get(wayNumber).get(setDecimal);
        String tagStored = block.getTag();

        // If there is no data stored
        if ((block.getValid() == 0 && block.getDirty() == 0) || block.getValid() == 1 && block.getDirty() == 0 && !tagStored.equals(tag)){
            block.setValid(1);  // Set valid to 1
            block.setTag(tag);  // Replace the tag

            if(isRead)
                block.setDirty(0);  // If the operation is read, set dirty to 0
            else
                block.setDirty(1); // If it's write, set dirty to 1

            numRead++;

            //System.out.println("(MISS) -> Index = " + setDecimal + " Tag = " + tag + " TagStored = " + tagStored);
        }
        // Different data in same index with dirty bit 1
        else if (block.getValid() == 1 && block.getDirty() == 1 && !tagStored.equals(tag)) {
            block.setValid(1);  // Set valid to 1
            block.setTag(tag);  // Replace the tag

            if(isRead)
                block.setDirty(0);  // If the operation is read, set dirty to 0
            else
                block.setDirty(1); // If it's write, set dirty to 1

            numWrite++;

            //System.out.println("(MISS - replaced - writeBACK) -> Index = " + setDecimal + " Tag = " + tag + " TagStored = " + tagStored);
        }
    }

    public void read (int setDecimal, String tag) {
        boolean flag = isHit(setDecimal, tag, true, false);   // Check if it's a hit

        // If it is not a hit, perform the miss operation
        if (!flag)
            miss(setDecimal, tag, true);
    }

    public void write (int setDecimal, String tag) {
        boolean flag = isHit(setDecimal, tag, false, false);   // Check if it's a hit

        // If it is not a hit, perform the miss operation
        if (!flag)
            miss (setDecimal, tag, false);
    }

    public void writeThrough (int setDecimal, String tag, String operationType) {
        boolean flag = isHit(setDecimal, tag, true, true);   // Check if it's a hit

        // If it is not a hit, perform the miss operation
        if (!flag) {
            // Update the trackers accordingly
            if (operationType.equals("R")) numRead++;
            else numWrite++;

            numMiss++;

            int wayNumber = findFIFO(setDecimal);    // Locate a block to use
            // Get the information for that block
            Block block = cache.get(wayNumber).get(setDecimal);
            String tagStored = block.getTag();

            // If there is no data stored or if the data is valid but the tags are not the same
            if (block.getValid() == 0 || (block.getValid() == 1 && !tagStored.equals(tag))){
                block.setValid(1);  // Set valid to 1
                block.setTag(tag);  // Update/replace the tag

                //System.out.println("(MISS) -> Index = " + setDecimal + " Tag = " + tag + " TagStored = " + tagStored);
            }
        }
    }

    private int findFIFO (int setDecimal) {
        List<Integer> listFIFO = FIFO.get(setDecimal);

        //System.out.println();
        //System.out.println("BEFORE = " + listFIFO);

        // Get the first available LRU
        int wayNumber = listFIFO.get(0);
        // Then remove it from the list
        listFIFO.remove(0);
        // Put it to the back of the list
        listFIFO.add(wayNumber);

        //System.out.println("AFTER = " + listFIFO);

        return wayNumber;
    }

    public int getNumHits() {
        return numHits;
    }

    public int getNumMiss() {
        return numMiss;
    }

    public int getNumRead() {
        return numRead;
    }

    public int getNumWrite() {
        return numWrite;
    }
}
