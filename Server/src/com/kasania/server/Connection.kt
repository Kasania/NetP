package com.kasania.server

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

open class Connection(private val socketChannel: SocketChannel, val type: Type) {

    enum class Type{
        DESKTOP,MOBILE
    }

    fun send(dataType: DataType, data: ByteBuffer){
        if (socketChannel.isConnected) {
            val packagedData = ByteBuffer.allocate(Character.BYTES + data.limit())
            packagedData.putChar(dataType.code)
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