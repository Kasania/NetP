package com.kasania.net;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum DataType {
    NO_TYPE('_'),
    LOGIN('L'),
    SYNC('S'),
    SYNCDone('D'),
    IMAGE('I'),
    AUDIO('A'),
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
    private final List<BiConsumer<UserInfo, byte[]>> receivers = new ArrayList<>();
    private Consumer<byte[]> sender;

    DataType(char code){
        this.code = code;
    }

    public void addReceiver(BiConsumer<UserInfo, byte[]> receiver){
        receivers.add(receiver);
    }

    public void received(UserInfo name, byte[] data){
        for (BiConsumer<UserInfo, byte[]> receiver : receivers) {
            receiver.accept(name,data);
        }
    }

    public void addSender(Consumer<byte[]> sender){
        this.sender = sender;
    }

    public void send(byte[] data){
        if(Objects.nonNull(sender))
            sender.accept(data);
    }

}
