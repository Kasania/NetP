package com.kasania.net;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Sender {


    private Consumer<ByteBuffer> sender;

    {
        DataType.LOGIN.addSender(bytes -> send(DataType.LOGIN,ByteBuffer.wrap(bytes)));
        DataType.TEXT.addSender(bytes -> send(DataType.TEXT,ByteBuffer.wrap(bytes)));
    }

    public void addSender(Consumer<ByteBuffer> sender){
        this.sender = sender;
    }

    private void send(DataType type, ByteBuffer data){

        ByteBuffer packagedData = ByteBuffer.allocate(Character.BYTES + data.limit());
        packagedData.putChar(type.code);
        packagedData.put(data);
        packagedData.flip();

        sender.accept(packagedData);
    }


}
