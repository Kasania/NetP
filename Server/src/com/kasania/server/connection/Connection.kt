package com.kasania.server.connection

import com.kasania.server.DataType
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean

open class Connection(val socketChannel: SocketChannel, private var type: Type) {

    enum class Type{
        DESKTOP,MOBILE,PENDING
    }

    private val datagramChannel = DatagramSocket()

    val syncDone : AtomicBoolean = AtomicBoolean(false)

    fun changeType(type: Type){
        this.type = type
    }
    fun getType(): Type {
        return this.type
    }
    private fun makePacket(dataType: DataType, src:Int, data: ByteBuffer): ByteBuffer? {

        val packagedData = ByteBuffer.allocate(Character.BYTES + Integer.BYTES + data.limit())
        packagedData.putChar(dataType.code)
        packagedData.putInt(src)
        packagedData.put(data)
        packagedData.flip()
        return packagedData

    }

    fun send(dataType: DataType, src:Int, data: ByteBuffer){
        makePacket(dataType,src,data)?.let { send(it) }
    }

    private fun send(data: ByteBuffer){
        if (socketChannel.isConnected) {
            val pendingData = ByteBuffer.allocate(Integer.BYTES + data.limit())
            pendingData.putInt(data.limit())
            pendingData.put(data)
            pendingData.flip()
            socketChannel.write(pendingData)
        }
    }

    fun sendImage(imagePort :Int, data: ByteBuffer){
        datagramChannel.send(DatagramPacket(data.array(), data.limit(), InetSocketAddress((socketChannel.remoteAddress as InetSocketAddress).address, imagePort)))
    }

    fun sendAudio(audioPort :Int, data: ByteBuffer){
        datagramChannel.send(DatagramPacket(data.array(), data.limit(), InetSocketAddress((socketChannel.remoteAddress as InetSocketAddress).address, audioPort)))
    }

    fun sendImage2(imagePort :Int, data: ByteBuffer){
        val packagedData = ByteBuffer.allocate(Character.BYTES + Integer.BYTES + data.limit())
        packagedData.putChar(DataType.IMAGE.code)
        packagedData.put(data)
        packagedData.flip()
        send(packagedData)
    }

    fun sendAudio2(audioPort :Int, data: ByteBuffer){
        val packagedData = ByteBuffer.allocate(Character.BYTES + Integer.BYTES + data.limit())
        packagedData.putChar(DataType.IMAGE.code)
        packagedData.put(data)
        packagedData.flip()
        send(packagedData)
    }


    fun syncDone(port:Int) {
        val buffer = ByteBuffer.allocate(4)
        buffer.putInt(port)
        buffer.flip()
        send(DataType.SYNCDone, 0, buffer)
    }
}