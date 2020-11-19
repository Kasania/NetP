package com.kasania.server.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum DataType {
    NO_TYPE('_'),
    LOGIN('L'),
    SYNC('S'),
    SYNCDone('D'),
    IMAGE('I'),
    UPDATE_USER('U'),
    TEXT('T')
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
    private BiConsumer<UserInfo, byte[]> received;
    private Consumer<byte[]> sender;

    DataType(char code){
        this.code = code;
    }

    public void addReceiver(BiConsumer<UserInfo, byte[]> receiver){
        this.received = receiver;
    }

    public void received(UserInfo name, byte[] data){
        received.accept(name,data);
    }

    public void addSender(Consumer<byte[]> sender){
        this.sender = sender;
    }

    public void send(byte[] data){
        if(Objects.nonNull(sender))
            sender.accept(data);
    }

}
