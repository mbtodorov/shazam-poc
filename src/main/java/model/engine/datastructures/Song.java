package main.java.model.engine.datastructures;

/** A simple data structure to represent a song
 *  It stores its name and whether its in the DB and in the folder
 */
public class Song {

    private String song;
    private boolean isInDB;
    private boolean isInDir;

    public Song(String song, boolean isInDB, boolean isInDir) {
        this.song = song;
        this.isInDB = isInDB;
        this.isInDir = isInDir;
    }

    public String getName() {
        return song;
    }

    public boolean isInDB() {
        return isInDB;
    }

    public boolean isInDir() {
        return isInDir;
    }

    public void setInDB() {
        isInDB = true;
    }
}
