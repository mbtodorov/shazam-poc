package main.java.model.engine.datastructures;

public class KeyPoint {
    private int time;
    private int frequency;

    public KeyPoint(int time, int frequency) {
        this.time = time;
        this.frequency = frequency;
    }

    public int getTime() {
        return time;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}