package com.kasania.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Receiver {
    private Supplier<ByteBuffer> reader;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final Map<Integer, String> names;

    public Receiver(){

        names = new ConcurrentHashMap<>();

        DataType.UPDATE_USER.addReceiver(this::updateNickname);
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
            byte[] data = new byte[buffer.limit() - Character.BYTES];
            buffer.get(data);

            DataType.getType(type).received(new UserInfo(0,""),data);

        }
    }

    private void updateNickname(UserInfo _unused, byte[] data){
        String[] value = new String(data).split("/");

        names.clear();

        for(int i = 0; i<value.length; i += 2){
            names.putIfAbsent(Integer.parseInt(value[i]),value[i+1]);
        }

    }

    void addReader(Supplier<ByteBuffer> reader){
        this.reader = reader;
    }

    void shutdown(){
        synchronized (this){
            isRunning.set(false);
        }
    }
}
