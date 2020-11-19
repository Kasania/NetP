package com.kasania.server.net;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Sender {


    private Consumer<ByteBuffer> sender;

    {

    }

    public void addSender(Consumer<ByteBuffer> sender){
        this.sender = sender;

    }

    private void send(DataType type, ByteBuffer data){

        ByteBuffer packagedData = ByteBuffer.allocate(Character.BYTES + data.limit());
        packagedData.putChar(type.code);
        packagedData.put(data);

        sender.accept(packagedData);
    }


}
