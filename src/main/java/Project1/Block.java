/*
Name: Joshua Samontanez
Course: EEL 4768 Summer 2022
Assignment title: Project 1 - Cache Simulator
*/
package Project1;

public class Block {
    private int valid;
    private int dirty;
    private String tag;

    public Block(int valid, int dirty, String tag) {
        this.valid = valid;
        this.dirty = dirty;
        this.tag = tag;
    }


    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public int getDirty() {
        return dirty;
    }

    public void setDirty(int dirty) {
        this.dirty = dirty;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
