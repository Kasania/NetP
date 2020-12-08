package com.kasania.server.connection

import com.kasania.server.DataType
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.security.SecureRandom


class DesktopConnection(socketChannel: SocketChannel, val name: String, val imagePort :Int, val  audioPort :Int) : Connection(socketChannel, Type.DESKTOP) {

    val connectionID : Int = (SecureRandom().nextInt(90000000) + 10000000)

    fun sendImage(data: ByteBuffer, src:Int){
        if(syncDone.get()){
            sendImage(imagePort, data)
        }
    }

    fun sendAudio(data: ByteBuffer, src:Int){
        if(syncDone.get()){
            sendAudio(audioPort, data)
        }
    }

    fun sendUserList(userList: String) {
        send(DataType.UPDATE_USER,0, ByteBuffer.wrap(userList.toByteArray(Charsets.UTF_8)))

    }

    fun sendSync(){
        val buffer = ByteBuffer.allocate(Integer.BYTES)
        buffer.putInt(connectionID)
        buffer.flip()
        send(DataType.SYNC,0, buffer)
    }


}