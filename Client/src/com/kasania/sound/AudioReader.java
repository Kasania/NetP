package com.kasania.sound;

import com.kasania.net.DataType;
import com.kasania.net.UserInfo;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioReader {
    int sampleRate = 44100;
    private final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
    private SourceDataLine speaker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AudioDataSet audioDataSet = new AudioDataSet();

    {
        try {

            speaker = AudioSystem.getSourceDataLine(format);
            speaker.open(format,3528);
            speaker.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        DataType.AUDIO.addReceiver(this::toSpeaker);
    }

    public void toSpeaker(UserInfo info, byte[] soundBytes) {
        executor.execute(()->{
            try {
                if(audioDataSet.contains(info.idx)){
                    byte[] data = audioDataSet.merge();
                    speaker.write(data,0, data.length);
                    audioDataSet.clear();
                }
                audioDataSet.put(info.idx, soundBytes);
            } catch (Exception e) {
                System.out.println("Error with audio playback: " + e);
                e.printStackTrace();
            }
        });
    }

    public void stop(){
        executor.shutdown();
        speaker.drain();
        speaker.stop();
        speaker.close();
    }

}
