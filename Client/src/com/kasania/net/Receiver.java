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
            int src = buffer.getInt();
            byte[] data = new byte[buffer.limit() - Character.BYTES - Integer.BYTES];
            buffer.get(data);
            if(src == 0){
                DataType.getType(type).received(new UserInfo(-1, "SERVER"),data);
            }
            else{
                int idx = 0;
                for (int i : names.keySet()) {
                    if (i == src) {
                        break;
                    }
                    ++idx;
                }
                DataType.getType(type).received(new UserInfo(idx, names.get(src)),data);

            }

        }
    }

    private void updateNickname(UserInfo _unused, byte[] data){
        String[] value = new String(data, StandardCharsets.UTF_8).split("//");

        names.clear();

        for (String s : value) {
            String[] userInfo = s.split("::");
            names.putIfAbsent(Integer.valueOf(userInfo[0]), userInfo[1]);
        }

        System.out.println(names);
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
