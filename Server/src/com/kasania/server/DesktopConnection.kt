package com.kasania.server

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class DesktopConnection(socketChannel: SocketChannel, val name: String) : Connection(socketChannel, Type.DESKTOP) {

    val connectionID : String = this.hashCode().toString()

    fun sendImage(data: ByteBuffer){
        send(DataType.IMAGE, data)
    }

    fun sendSync(){
        send(DataType.SYNC, ByteBuffer.wrap(connectionID.toByteArray(Charsets.UTF_8)))

    }

}