package com.kasania.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public final class Connection {

    private SocketChannel socketChannel;
    private DatagramSocket imageSocket;
    private DatagramSocket audioSocket;

    private ExecutorService executorService;
    private FutureTask<?> task;


    private ExecutorService executorService2;
    private FutureTask<?> task2;


    private ExecutorService executorService3;
    private FutureTask<?> task3;

    private Sender sender;
    private Receiver receiver;

    public static int VideoPort;
    public static int AudioPort;

    public void connect(String address, int port, int imagePort, int audioPort){

        VideoPort = imagePort;
        AudioPort = audioPort;

        new Thread(() ->{

            try {
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(address,port));

                imageSocket = new DatagramSocket(new InetSocketAddress(VideoPort));
                audioSocket = new DatagramSocket(new InetSocketAddress(AudioPort));

                sender = new Sender();
                receiver = new Receiver();

                receiver.addReader(this::read);
                receiver.addImageReader(this::readImage);
                receiver.addAudioReader(this::readAudio);
                sender.addSender(this::write);

                executorService = Executors.newSingleThreadExecutor();
                task = new FutureTask<>(receiver::readData);
                executorService.execute(task);

                executorService2 = Executors.newSingleThreadExecutor();
                task2 = new FutureTask<>(receiver::readImageData);
                executorService2.execute(task2);

                executorService3 = Executors.newSingleThreadExecutor();
                task3 = new FutureTask<>(receiver::readAudioData);
                executorService3.execute(task3);

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

    ByteBuffer readImage(){

        ByteBuffer data = ByteBuffer.allocate(16384);

        try {
            byte[] buffer = new byte[16384];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            imageSocket.receive(datagramPacket);
            data.put(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data.flip();
    }

    ByteBuffer readAudio(){

        ByteBuffer data = ByteBuffer.allocate(3528 + Integer.BYTES);

        try {

            byte[] buffer = new byte[3528 + Integer.BYTES];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 3528 + Integer.BYTES);
            audioSocket.receive(datagramPacket);
            data.put(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data.flip();
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
