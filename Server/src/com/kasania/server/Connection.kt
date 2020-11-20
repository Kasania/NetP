package com.kasania.server

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

open class Connection(val socketChannel: SocketChannel, private var type: Type) {

    enum class Type{
        DESKTOP,MOBILE,PENDING
    }

    fun changeType(type: Type){
        this.type = type
    }
    fun getType():Type{
        return this.type
    }

    fun send(dataType: DataType, data: ByteBuffer){
        send(dataType,0,data)
    }

    fun send(dataType: DataType, src:Int, data: ByteBuffer){
        if (socketChannel.isConnected) {
            val packagedData = ByteBuffer.allocate(Character.BYTES + Integer.BYTES + data.limit())
            packagedData.putChar(dataType.code)
            packagedData.putInt(src)
            packagedData.put(data)
            packagedData.flip()

            val pendingData = ByteBuffer.allocate(Integer.BYTES + packagedData.limit())
            pendingData.putInt(packagedData.limit())
            pendingData.put(packagedData)
            pendingData.flip()
            socketChannel.write(pendingData)
        }

    }

    fun syncDone() {
        send(DataType.SYNCDone, ByteBuffer.wrap("".toByteArray()));
    }
}