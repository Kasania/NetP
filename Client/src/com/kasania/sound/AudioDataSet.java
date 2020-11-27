package com.kasania.sound;

import java.util.ArrayList;
import java.util.List;

public class AudioDataSet {

    private final List<Integer> audioSourceList = new ArrayList<>();
    private final List<byte[]> audioDataList = new ArrayList<>();

    public void put(int src, byte[] data){
        audioSourceList.add(src);
        audioDataList.add(data);
    }

    public boolean contains(int src){
        for (int audioSource : audioSourceList) {
            if(audioSource == src){
                return true;
            }
        }
        return false;
    }

    public byte[] merge(){
        byte[] data = new byte[3528];
        byte size = (byte) audioDataList.size();
        for (byte[] audioData : audioDataList) {
            for(int i = 0; i<data.length; ++i){
                data[i] += audioData[i] / size;
            }
        }
        return data;
    }

    public void clear(){
        audioDataList.clear();
    }

}
