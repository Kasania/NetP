package com.kasania.server.connection

import com.kasania.server.DataType
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class MobileConnection(socketChannel: SocketChannel, val connectionID : Int) : Connection(socketChannel, Type.MOBILE) {

    fun syncCancel(){
        val buffer = ByteBuffer.allocate(Integer.BYTES)
        buffer.putInt(connectionID)
        buffer.flip()
        send(DataType.SYNCCancel,0, buffer)
    }
}