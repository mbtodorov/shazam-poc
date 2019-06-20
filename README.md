# Shazam Proof of Concept
I have attempted to recreate the functionality of the infamous 'Shazam' app. For those who don't know,
it is an app used to recognize audio. It has a database of millions of precomputed songs and it takes just seconds
to match the crappy audio from your phone's microphone while bypassing side noise in a matter of seconds! It is
like magic!
<br>
My take is an entirely Java-based solution, in which I have recreated the client-server communication the app has
. 

# How I did it
I found an article which was extremely helpful and pretty much followed all of the steps from it: http://coding-geek.com/how-shazam-works/
. If you are looking for a more detailed explanation, please do reference the article. as this is more or less a summarized
version of it. <br> <br> First I pre-compute all songs in
the {root}/music dir (works only for .wav files. Then populate a small database with the 
fingerprints generated from the .wav files. Then you can either chose to select a file or use the microphone.
<br> 

### Populating DB with fingerprints of songs

* #### Get all .wav files
&emsp; Look for all .wav files in the /music folder and start decoding each one in a separate thread. 

* ####  Apply a low-pass filter for frequencies > 5 kHz
&emsp; I used this lib: http://www.source-code.biz/dsp/java/ to apply a low-pass filter. Check 
AudioUtils class for details on the filter.

* #### Convert to mono
&emsp; I convert a raw byte array containing the audio file (excluding the header of the .wav). To achieve that I used
a bit-wise compression algorithm which, again, can be found in the AudioUtils class. It takes the average of every two bytes.
```$xslt
private static byte[] doubleBitWiseCompression(byte[] in) {
    byte[] out = new byte[(int) Math.ceil(in.length/2)];
    for (int i = 0 ; i < out.length/2; ++i){
        int left  = (in[i * 4 + 1]     << 8) | (in[i * 4]     & 0xff);
        int right = (in[i * 4 + 2 + 1] << 8) | (in[i * 4 + 2] & 0xff);
        int avg = (left + right) / 2;
        out[i * 2 + 1] = (byte) ((avg >> 8) & 0xff);
        out[i * 2]     = (byte)  (avg       & 0xff);
    }
    return out;
}
```

* #### Down-sample to 11025 Hz
&emsp; To down-sample the raw byte array I take the average of every 4 bytes into one (because I want to down-sample to 11025 Hz which is exactly 4 times
less than the original 44.1 kHz sampling rate - check the aforementioned article for reference). This is actually
using the exact same algorithm as for the stereo to mono conversion, but twice.  

* ####  Apply FFT with window size 1024
&emsp; I used an FFT algorithm which works with doubles rather use the domain of complex numbers. The results
give me a 2D double array, where the first dimension is ~100ms from the song, the second dimension is a frequency bin between 0 and 512 (each bin is 10.76Hz) and the value 
is the amplitude. The spectrogram can be displayed by clicking the song button of the song (for testing purposes).

* #### Extract key points from FFT result
&emsp; The result of the FFT is essentially my spectrogram. From it i extract key-points. The algorithm aims to
keep points which are strongest in the vicinity. This helps with noise robustness.

* #### Generate fingerprints
&emsp; This is probably the hardest bit, because there is not a lot of useful information on the internet. I have
stuck to the idea of target zones, but my implementation of fingerprinting is very much improvised. I end up with ~ 500 longs for each second of a song.

* #### Populate DB with fingerprints.
&emsp; Insert all fingerprints in a DB and match them with a song id. There is a separate table which matches song id's with song names

### Matching mic input with fingerprints in DB
&emsp; TODO
#### 1. Start receiving mic input stream
&emsp; TODO
#### 2. For every second of mic input, start a new thread and begin matching
&emsp; TODO
#### 3. Down-sample to 11025 Hz
&emsp; TODO
#### 4. Convert raw byte array to raw double array
&emsp; TODO
#### 5. Apply FFT with window size 1024
&emsp; TODO
#### 6. Extract key points from FFT result
&emsp; TODO
#### 7. Hash key points
&emsp; TODO
#### 8. Match with DB
&emsp; TODO

# How to run the code
### VM options for Intellij
```
-p
"C:\Program Files\Java\javafx-sdk-11.0.2\lib"
--add-modules=javafx.controls,javafx.fxml
--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED
--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED
--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED
```
### Setup a MySQL local database
&emsp; TODO
# References
&emsp; TODO