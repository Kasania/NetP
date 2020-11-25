package com.kasania.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Receiver {
    private Supplier<ByteBuffer> reader;
    private Supplier<ByteBuffer> imageReader;
    private Supplier<ByteBuffer> audioReader;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isImageReceiveRunning = new AtomicBoolean(false);
    private final AtomicBoolean isAudioReceiveRunning = new AtomicBoolean(false);



    public Receiver(){


    }

    boolean readData(){
        isRunning.set(true);

        while(true){
            synchronized (this){
                if(!isRunning.get()){
                    return false;
                }
            }

            ByteBuffer buffer = reader.get();
            char type = buffer.getChar();
            int src = buffer.getInt();
            byte[] data = new byte[buffer.limit() - Character.BYTES - Integer.BYTES];
            buffer.get(data);
            if(src == 0){
                DataType.getType(type).received(new UserInfo(-1, "SERVER"),data);
            }
            else{
                DataType.getType(type).received(UserInfo.names.get(src),data);

            }

        }
    }


    boolean readImageData(){
        isImageReceiveRunning.set(true);

        while(true){
            synchronized (this){
                if(!isImageReceiveRunning.get()){
                    return false;
                }
            }

            ByteBuffer buffer = imageReader.get();
            byte[] data = new byte[buffer.limit() - Integer.BYTES];
            int src = buffer.getInt();
            buffer.get(data);

            DataType.IMAGE.received(UserInfo.names.get(src),data);

        }
    }

    boolean readAudioData(){
        isAudioReceiveRunning.set(true);

        while(true){
            synchronized (this){
                if(!isAudioReceiveRunning.get()){
                    return false;
                }
            }

            ByteBuffer buffer = audioReader.get();
            int src = buffer.getInt();
            byte[] data = new byte[buffer.limit() - Integer.BYTES];
            buffer.get(data);

            DataType.AUDIO.received(UserInfo.names.get(src), data);

        }
    }



    void addReader(Supplier<ByteBuffer> reader){
        this.reader = reader;
    }
    void addImageReader(Supplier<ByteBuffer> reader){
        this.imageReader = reader;
    }

    void addAudioReader(Supplier<ByteBuffer> reader){
        this.audioReader = reader;
    }

    void shutdown(){
        synchronized (this){
            isRunning.set(false);
        }
    }
}