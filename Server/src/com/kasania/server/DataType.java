package com.kasania.server;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.BiConsumer;

public enum DataType {

    LOGIN('L'),
    TEXT('T'),

    UPDATE_USER('U'),

    SYNC('S'),
    SYNCDone('D'),
    SYNCFail('F'),

    IMAGE('I'),
    AUDIO('A'),
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
    private BiConsumer<DatagramChannel, ByteBuffer> received2;

    DataType(char code){
        this.code = code;
    }

    public void addReceiver(BiConsumer<SocketChannel, byte[]> receiver){
        this.received = receiver;
    }


    public void addUDPReceiver(BiConsumer<DatagramChannel, ByteBuffer> receiver){
        this.received2 = receiver;
    }

    public void received(SocketChannel channel, byte[] data){
        received.accept(channel,data);
    }

    public void receivedUDP(DatagramChannel channel, ByteBuffer data){
        received2.accept(channel,data);
    }

}
