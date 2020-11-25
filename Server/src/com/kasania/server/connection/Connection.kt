package com.kasania.server.connection

import com.kasania.server.DataType
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel

open class Connection(val socketChannel: SocketChannel, private var type: Type) {

    enum class Type{
        DESKTOP,MOBILE,PENDING
    }

    private val datagramChannel = DatagramChannel.open()

    fun changeType(type: Type){
        this.type = type
    }
    fun getType(): Type {
        return this.type
    }
    private val imageDataAddress = InetSocketAddress((socketChannel.remoteAddress as InetSocketAddress).address,11114)
    private val audioDataAddress = InetSocketAddress((socketChannel.remoteAddress as InetSocketAddress).address,11115)
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

    fun sendImage(data: ByteBuffer){
        datagramChannel.send(data, imageDataAddress)
    }

    fun sendAudio(data: ByteBuffer){
        datagramChannel.send(data, audioDataAddress)
    }


    fun syncDone() {
        send(DataType.SYNCDone, 0, ByteBuffer.wrap("".toByteArray()));
    }
}