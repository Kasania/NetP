package com.kasania.netp

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Connection {

    val TYPE_VERIFICATION = 'V'
    val TYPE_IMAGE = 'I'

    private val executorService : ExecutorService = Executors.newSingleThreadExecutor();
    private lateinit var address : String

    private var port : Int = 0

    private lateinit var socketChannel :SocketChannel

    fun connect(address: String, port: Int) {
        this.address = address
        this.port = port
        executorService.submit(this::establishConnection);
    }

    fun disconnect(){
        executorService.shutdown();
    }

    private fun establishConnection(){
        socketChannel = SocketChannel.open()
        socketChannel.connect(InetSocketAddress(address, port))
    }

    fun sendVerificationCode(code: String){
        val codeBytes = StandardCharsets.UTF_8.encode(code)
        val packagedData = ByteBuffer.allocate(2 + codeBytes.limit())
        packagedData.putChar(TYPE_VERIFICATION)
        packagedData.put(codeBytes)
        send(packagedData)
    }

    fun sendImage(bitmap: Bitmap){
        val bitmapBytes : ByteBuffer
        ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            bitmapBytes = ByteBuffer.wrap(this.toByteArray())
        }
        val packagedData = ByteBuffer.allocate(2 + bitmapBytes.limit())
        packagedData.putChar(TYPE_IMAGE)
        packagedData.put(bitmapBytes)
        send(packagedData)
    }

    private fun send(byteBuffer: ByteBuffer){
        if (socketChannel.isConnected) {
            val data = ByteBuffer.allocate(4 + byteBuffer.limit())
            data.putInt(byteBuffer.limit())
            data.put(byteBuffer)
            data.flip()
            socketChannel.write(data)
        }
    }

    private fun read(){

    }




}