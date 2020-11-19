package com.kasania.server;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.BiConsumer;

public enum DataType {
    NO_TYPE('_'),
    LOGIN('L'),
    SYNC('S'),
    SYNCDone('D'),
    SYNCFail('F'),
    IMAGE('I'),
    UPDATE_USER('U'),
    TEXT('T'),

    VERIFY('V')
    ;
    private static final Map<Character, DataType> types = new HashMap<>();

    static{
        for(DataType type : DataType.values()) {
            types.put(type.code, type);
        }
    }

    public static DataType getType(char code){
        return types.get(code);
    }


    public final char code;
    private BiConsumer<SocketChannel, byte[]> received;

    DataType(char code){
        this.code = code;
    }

    public void addReceiver(BiConsumer<SocketChannel, byte[]> receiver){
        this.received = receiver;
    }

    public void received(SocketChannel channel, byte[] data){
        received.accept(channel,data);
    }

}
