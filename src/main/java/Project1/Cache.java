/*
Name: Joshua Samontanez
Course: EEL 4768 Summer 2022
Assignment title: Project 1 - Cache Simulator
*/
package Project1;

import java.util.*;

public class Cache {
    private final int assoc;

    private int numHits = 0;
    private int numMiss = 0;
    private int numRead = 0;
    private int numWrite = 0;

    private HashMap<Integer, ArrayList<Integer>> LRU;

    private final HashMap<Integer, HashMap<Integer, Block>> cache;

    public Cache(int assoc, int numSets) {
        this.assoc = assoc;

        //Cache construction
        cache = new HashMap<>();
        for (int i = 0; i < assoc; i++) {
            // Create each sets
            HashMap<Integer, Block> set = new HashMap<>();
            // Create LRUs for each sets
            LRU = new HashMap<>();
            for(int j = 0; j < numSets; j++) {
                // Populate the LRUs, initialize by adding all numbers less than "assoc"
                ArrayList<Integer> listLRU = initializeList();

                LRU.put(j, listLRU);


                // Populate the blocks for each sets
                Block block = new Block(0, 0, null);
                set.put(j, block);
            }
            // Populate each associativity
            cache.put(i, set);
        }
    }

    public void read (int setDecimal, String tag) {
        boolean flag = isHit(setDecimal, tag, true, false);    // Check if it's a hit

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

    private boolean isHit(int setDecimal, String tag, boolean isRead, boolean isWriteThru) {
        // Loop through all the sets in a cache to find a hit
        for (int i = 0; i < assoc; i++){
            // Get the block info
            Block block = cache.get(i).get(setDecimal);
            String tagStored = block.getTag();
            int valid = block.getValid();

            // If a hit is found
            if (valid == 1 && tagStored.equals(tag)) {
                // Get the LRU and MRU for the corresponding sets
                ArrayList<Integer> listLRU = LRU.get(setDecimal);

                /*
                System.out.println("\nBEFORE");
                System.out.println("Hit at way " + i);
                System.out.println(listLRU);

                 */

                int index = findIndex(listLRU, i);
                listLRU.remove(index);
                listLRU.add(i);

                // Only applies if it's a write-back
                if(!isWriteThru) {
                   if(isRead)
                       block.setDirty(0);  // If the operation is read, set dirty to 0
                   else
                       block.setDirty(1); // If it's write, set dirty to 1
                }


                block.setValid(1); // Then set it to valid

                // Update the hashmap
                LRU.put(setDecimal, listLRU);

                /*
                System.out.println("AFTER");
                System.out.println("Hit at way " + i);
                System.out.println(listLRU);

                 */

                // Increase the hits and the memory reads
                numHits++;
                return true;
            }
        }
        return false;
    }

    private void miss (int setDecimal, String tag, boolean isRead) {
        numRead++;
        numMiss++;
        int wayNumber = findLRU(setDecimal);    // Locate a block to use

        // Get the block information
        Block block = cache.get(wayNumber).get(setDecimal);
        String tagStored = block.getTag();

        // If there is no data stored or the date stored is not the same
        if ((block.getValid() == 0 && block.getDirty() == 0) || (block.getValid() == 1 && block.getDirty() == 0 && !tagStored.equals(tag))){
            block.setValid(1);  // Set valid to 1
            block.setTag(tag);  // Replace the tag

            if(isRead)
                block.setDirty(0);  // If the operation is read, set dirty to 0
            else
                block.setDirty(1); // If it's write, set dirty to 1


        }
        // Different data in same index with dirty bit 1 (Write-back)
        else if (block.getValid() == 1 && block.getDirty() == 1 && !tagStored.equals(tag)) {
            block.setValid(1);  // Set valid to 1
            block.setTag(tag);  // Replace the tag

            if(isRead){
                block.setDirty(0);  // If the operation is read, set dirty to 0
            }
            else
                block.setDirty(1); // If it's write, set dirty to 1

            numWrite++;
        }


    }

    public void writeThrough (int setDecimal, String tag, String operationType) {
        // Update the trackers accordingly
        if (operationType.equals("W")) numWrite++;

        boolean flag = isHit(setDecimal, tag, true, true);   // Check if it's a hit
        // If it is not a hit, perform the miss operation
        if (!flag) {
            numRead++;
            numMiss++;

            int wayNumber = findLRU(setDecimal);    // Locate a block to use
            // Get the information for that block
            Block block = cache.get(wayNumber).get(setDecimal);
            String tagStored = block.getTag();

            // If there is no data stored or if the data is valid but the tags are not the same
            if (block.getValid() == 0 || (block.getValid() == 1 && !tagStored.equals(tag))){
                block.setValid(1);  // HashSet valid to 1
                block.setTag(tag);  // Update/replace the tag
            }
        }
    }

    private ArrayList<Integer> initializeList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < assoc; i++) {
            list.add(i);
        }
        return list;
    }


    private int findLRU (int setDecimal) {
        ArrayList<Integer> listLRU = LRU.get(setDecimal);

        // Get the first available number from the LRU
        Integer wayNumber = listLRU.get(0);
        // Remove that element from the list, then update the LRU
        listLRU.remove(wayNumber);
        listLRU.add(wayNumber);

        LRU.put(setDecimal, listLRU);

        /*
        System.out.println();
        System.out.println(listLRU);

         */

        return wayNumber;
    }

    private int findIndex (ArrayList<Integer> listLRU, int wayNumber){
        for (int i = 0; i < listLRU.size(); i++) {
            // FOUND
            if (listLRU.get(i) == wayNumber) {
                return i;
            }
        }
        // NOT FOUND
        return -1;
    }


    public int getNumWrite() {
        return numWrite;
    }

    public int getNumRead() {
        return numRead;
    }

    public int getNumMiss() {
        return numMiss;
    }

    public int getNumHits() {
        return numHits;
    }
}
