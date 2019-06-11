# Shazam Proof of Concept
I have attempted to recreate the functionality of the infamous 'Shazam' app. For those who don't know,
it is an app used to recognize audio. It has a database of millions of precomputed songs and it takes just seconds
to match the crappy audio from your phone's microphone while bypassing side noise in a matter of seconds! It is
like magic!
<br>
My take is an entirely Java-based solution. I have recreated the client-server interaction in the original product.
I have very closely followed a great article on how 'Shazam' works and I highly recommend reading to fully
comprehend the code I have. Here is a link: http://coding-geek.com/how-shazam-works/

#How I did it
I found the article extremely helpful and pretty much followed all of the steps from it. First I pre-compute all songs in
the {root}/music dir (works only for .wav files). I populate a small database with the fingerprints generated from the .wav files
and I later match mic input to those fingerprints.

####Generating fingerprints for songs

##### ⋅⋅* Get a .wav file
&emsp; Look for all .wav files in the /music folder and return an String[] with all of their names
The array is used to init a button for each element and call a decode method on the file it represents. The 
steps below are all executed inside the AudioDecode.decodeFile() method. It is a controller class which
has only static methods which themselves execute static methods in a specific order. The reason for this is
multi-thread capability. Static methods are natively thread-safe and I am using multi-threading to speed up execution time.

#####2. Apply a low-pass filter for frequencies > 5 kHz
&emsp; I used this lib: http://www.source-code.biz/dsp/java/ to apply a low-pass filter. Check 
AudioUtils class for details on the filter.

#####3. Convert to mono
&emsp; I convert a raw byte array containing the audio file (excluding the header of the .wav). To achieve that I used
a bitWiseCompression algorithm which, again, can be found in the AudioUtils class. It takes the average of every two bytes.
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

#####4. Down-sample to 11025 Hz
&emsp; To down-sample the raw byte array I take the average of every 4 bytes into one (because I want to down-sample to 11025 Hz which is exactly 4 times
less than the original 44.1 kHz sampling rate - check the aforementioned article for reference). This is actually
using the exact same algorithm as for the stereo to mono conversion, but twice.  

#####5. Convert raw byte array to raw double array
&emsp; I found no implementation of the FFT algorithm which work with byte arrays so I had to convert it to a double array using bitwise operators.

#####6. Apply FFT with window size 1024
&emsp; Finally I am ready to apply an FFT and get a double[][] which can be used to draw a spectrogram of the song.
This is also a feature I implemented for testing purposes - how else was I supposed to know if everything works.
The decodeSong() algorithm returns the FFT result which is then passed along to the songBtn for the respective song.
When pressed the button will pop-up a new window containing an image of the spectrogram as well as extracted key points.

#####7. Extract key points from FFT result
&emsp; TODO
#####8. Hash key points
&emsp; TODO
#####8. Populate DB with fingerprints for song
&emsp; TODO

####Matching mic input with fingerprints in DB
&emsp; TODO
#####1. Start receiving mic input stream
&emsp; TODO
#####2. For every second of mic input, start a new thread and begin matching
&emsp; TODO
#####3. Down-sample to 11025 Hz
&emsp; TODO
#####4. Convert raw byte array to raw double array
&emsp; TODO
#####5. Apply FFT with window size 1024
&emsp; TODO
#####6. Extract key points from FFT result
&emsp; TODO
#####7. Hash key points
&emsp; TODO
#####8. Match with DB
&emsp; TODO

#How to run the code
#### VM options for Intellij
```
-p
"C:\Program Files\Java\javafx-sdk-11.0.2\lib"
--add-modules=javafx.controls,javafx.fxml
--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED
--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED
--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED
```
#### Setup a MySQL local database
&emsp; TODO
#References
&emsp; TODO