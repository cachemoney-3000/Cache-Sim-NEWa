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

    private HashMap<Integer, Set<Integer>> LRU;
    private HashMap<Integer, Set<Integer>> MRU;

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
            MRU = new HashMap<>();
            for(int j = 0; j < numSets; j++) {
                // Populate the LRUs, initialize by adding all numbers less than "assoc"
                Set<Integer> listLRU = initializeList();
                // Create an empty list for MRU
                Set<Integer> listMRU = new HashSet<>();
                LRU.put(j, listLRU);
                MRU.put(j, listMRU);

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
                Set<Integer> listLRU = LRU.get(setDecimal);
                Set<Integer> listMRU = MRU.get(setDecimal);

                // If the LRU list is empty, re-initialize it again using the MRU data
                if (listLRU.isEmpty())
                    listLRU = copyFromMRU(setDecimal, listMRU);

                listLRU.remove(i);  // Remove the specific index where we find the hit from LRU
                listMRU.add(i); // And add that index to the MRU

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
                MRU.put(setDecimal, listMRU);

                /*
                // ~ D E B U G G I N G ~
                System.out.println();
                System.out.println(listMRU);
                System.out.println(listLRU);
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

            numRead++;

            //System.out.println("(MISS) -> Index = " + setDecimal + " Tag = " + tag + " TagStored = " + tagStored);
        }
        // Different data in same index with dirty bit 1 (Write-back)
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

    public void writeThrough (int setDecimal, String tag, String operationType) {
        boolean flag = isHit(setDecimal, tag, true, true);   // Check if it's a hit

        // If it is not a hit, perform the miss operation
        if (!flag) {
            // Update the trackers accordingly
            if (operationType.equals("R")) numRead++;
            else numWrite++;

            numMiss++;

            int wayNumber = findLRU(setDecimal);    // Locate a block to use
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

    private Set<Integer> initializeList() {
        Set<Integer> list = new HashSet<>();
        for (int i = 0; i < assoc; i++) {
            list.add(i);
        }
        return list;
    }

    private Set<Integer> copyFromMRU(int setDecimal, Set<Integer> listMRU) {
        // Copy the MRU list to LRU
        Set<Integer> listLRU = new HashSet<>(listMRU);
        LRU.put(setDecimal, listLRU);

        // Clear the MRU list
        listMRU.clear();
        MRU.put(setDecimal, listMRU);

        return listLRU;
    }

    private int findLRU (int setDecimal) {
        Set<Integer> listLRU = LRU.get(setDecimal);
        Set<Integer> listMRU = MRU.get(setDecimal);

        // If the LRU list is empty, re-initialize it again by copying the data from MRU
        if (listLRU.isEmpty())
            listLRU = copyFromMRU(setDecimal, listMRU);

        // Get the first available number from the LRU
        Integer wayNumber = listLRU.stream().findFirst().get();
        // Remove that element from the list, then update the LRU
        listLRU.remove(wayNumber);
        LRU.put(setDecimal, listLRU);

        // Add the number that we removed from LRU to MRU, then update the MRU
        listMRU.add(wayNumber);
        MRU.put(setDecimal, listMRU);

        /*
        // ~ D E B U G G I N G ~
        System.out.println();
        System.out.println(listMRU);
        System.out.println(listLRU);
        System.out.println("LRU NUMBER " + wayNumber);
        // ~ D E B U G G I N G ~

         */

        return wayNumber;
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
