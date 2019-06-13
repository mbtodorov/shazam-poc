package main.java.model.matching.microphone;

import main.java.model.exc.NoMatchException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MicListener extends Thread {
    private final static Logger logger = Logger.getLogger(MicListener.class.getName());

    // static strings for exception descriptions
    private static final String MATCH_FOUND, NO_MATCH_FOUND;
    static {
        MATCH_FOUND = "Success! A match has been found: ";
        NO_MATCH_FOUND = "No match has been found. Try Again.";
    }

    private Exception ex;
    private AudioFormat format;

    MicListener(AudioFormat format) {
        this.format = format;
    }

    @Override
    public void run() {
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        try {
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            logger.log(Level.INFO, "Connected successfully.");
            targetLine.start();
            logger.log(Level.INFO, "Started targetLine...");

            int count;
            byte[] targetData = new byte[targetLine.getBufferSize() / 5];

            long t = System.currentTimeMillis();
            long end = t + 3000;
            logger.log(Level.INFO, "Listening...");

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            while (System.currentTimeMillis() < end) {
                count = targetLine.read(targetData, 0, targetData.length);

                if(count > 0) {
                    out.write(targetData, 0, count);
                }
            }

            byte[] b = out.toByteArray();
            System.out.println("Mic input: ");
            for (byte value : b) {
                System.out.print((int) value + " ");
            }
            out.close();
            targetLine.close();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Line was not available... Terminating system.");
            System.exit(-1);
        }

        if(ex == null) {
            ex = new NoMatchException(NO_MATCH_FOUND);
        }
    }

    void checkForException() throws Exception {
        if (ex!= null) {
            throw ex;
        }
    }
}
