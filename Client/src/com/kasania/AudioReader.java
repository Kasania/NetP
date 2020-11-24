package com.kasania;

import com.kasania.net.DataType;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioReader {
    int sampleRate = 44100;
    private final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
    private SourceDataLine speaker;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    {

        try {
            speaker = AudioSystem.getSourceDataLine(format);

            speaker.open(format,3528);
            speaker.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        DataType.AUDIO.addReceiver((info, bytes) -> toSpeaker(bytes));
    }

    public void toSpeaker(byte[] soundBytes) {
        executor.execute(()->{
            try {

                speaker.write(soundBytes,0, 3528);

            } catch (Exception e) {
                System.out.println("Error with audio playback: " + e);
                e.printStackTrace();
            }
        });
    }

}
