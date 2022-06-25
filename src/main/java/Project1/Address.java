/*
Name: Joshua Samontanez
Course: EEL 4768 Summer 2022
Assignment title: Project 1 - Cache Simulator
*/
package Project1;

import java.math.BigInteger;

public class Address {
    private final String tagHex;
    private final int setDecimal;

    public Address (String address, int offsetBits, int setBits) {
        // Convert the hex address to binary
        String binary = hexToBinary(address);

        // Get the number of bits for the address
        int addressBits = binary.length();
        // Find the number of bits for tag
        int tagBits = addressBits - (offsetBits + setBits);

        // Get rid of the offset
        String binaryOffsetRemoved = removeOffset(binary, offsetBits);
        // Get the tag using the tagBits
        String tag = getTag(binaryOffsetRemoved, tagBits);
        // Get the set using the setBits
        String set = getSet(binaryOffsetRemoved, setBits);

        tagHex = binaryToHex(tag);   // Convert tag from binary to hex
        setDecimal = Integer.parseInt(binaryToDec(set));   // Convert set from binary to decimal
    }

    // GETTERS
    public int getSetDecimal() {
        return setDecimal;
    }
    public String getTagHex() {
        return tagHex;
    }

    // Removes the offset from the address
    private static String removeOffset (String binary, int offsetBits) { return binary.substring(0, binary.length() - offsetBits); }

    // Converts Binary to Decimal
    private static String binaryToDec (String binary) { return new BigInteger(binary, 2).toString(10); }

    // Get the tag using tag bits
    private static String getTag (String binary, int tagBits) {
        return binary.substring(0, tagBits);
    }

    // Get the set using set bits
    private static String getSet (String binary, int setBits) {
        return binary.substring(binary.length() - setBits);
    }

    // Converts Binary to Hex
    private static String binaryToHex (String binary) {
        return new BigInteger(binary, 2).toString(16);
    }

    private static String hexToBinary (String hex) {
        hex = hex.substring(2); // Remove the "0x" at the beginning of each addresses
        return new BigInteger(hex, 16).toString(2);
    }

}
