package com.kasania.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public final class Connection {

    private SocketChannel socketChannel;

    private ExecutorService executorService;
    private FutureTask<?> task;

    private Sender sender;
    private Receiver receiver;

    public void connect(String address, int port){

        new Thread(() ->{

            try {
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(address,port));

                sender = new Sender();
                receiver = new Receiver();

                receiver.addReader(this::read);
                sender.addSender(this::write);

                executorService = Executors.newSingleThreadExecutor();
                task = new FutureTask<>(receiver::readData);
                executorService.execute(task);

            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
            }
        }).start();
    }

    ByteBuffer read(){

        ByteBuffer size = ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer data = null;

        try {
            socketChannel.read(size);
            size.flip();
            int sz = size.getInt();
            data = ByteBuffer.allocate(sz);
            while(data.hasRemaining()){
                socketChannel.read(data);
            }
        }
        catch(ClosedByInterruptException ex){
            //Disconnected
        }
        catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
        Objects.requireNonNull(data).flip();
        return data;
    }

    void write(ByteBuffer byteBuffer){
        try {
            if(socketChannel.isConnected()){
                ByteBuffer data = ByteBuffer.allocate(Integer.BYTES + byteBuffer.limit());
                data.putInt(byteBuffer.limit());
                data.put(byteBuffer);
                data.flip();
                socketChannel.write(data);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){

        try {
            receiver.shutdown();
            task.cancel(true);
            executorService.shutdown();

            sender.addSender((_closed)->{});
            socketChannel.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
