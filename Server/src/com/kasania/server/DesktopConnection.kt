package com.kasania.server

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean


class DesktopConnection(socketChannel: SocketChannel, val name: String) : Connection(socketChannel, Type.DESKTOP) {

    val connectionID : Int = (SecureRandom().nextInt(90000000) + 10000000)

    val syncDone : AtomicBoolean = AtomicBoolean(false)

    fun sendImage(data: ByteBuffer, src:Int){
        if(syncDone.get()){
            send(DataType.IMAGE, src, data)
        }
    }

    fun sendUserList(userList: String) {
        send(DataType.UPDATE_USER, ByteBuffer.wrap(userList.toByteArray(Charsets.UTF_8)))

    }

    fun sendSync(){
        val buffer = ByteBuffer.allocate(Integer.BYTES)
        buffer.putInt(connectionID)
        buffer.flip()
        send(DataType.SYNC, buffer)
    }

}