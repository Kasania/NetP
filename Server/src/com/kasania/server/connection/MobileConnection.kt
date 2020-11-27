package com.kasania.server.connection

import com.kasania.server.DataType
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel

class MobileConnection(socketChannel: SocketChannel, val connectionID: Int) : Connection(socketChannel, Type.MOBILE) {

    private lateinit var imageSocket : DatagramChannel

    fun syncCancel(){
        val buffer = ByteBuffer.allocate(Integer.BYTES)
        buffer.putInt(connectionID)
        buffer.flip()
        send(DataType.SYNCCancel, 0, buffer)
    }


    fun prepareAudio():Int{
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        imageSocket = DatagramChannel.open()
        imageSocket.bind(InetSocketAddress(port))

        return port
    }


    fun runAudioRead(){

        Thread{

            while(syncDone.get()){
                val buffer = ByteBuffer.allocate(3528 + Int.SIZE_BYTES)
                imageSocket.receive(buffer)
                DataType.AUDIO.receivedUDP(0, buffer)

            }


        }.start()

    }

}