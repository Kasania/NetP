package com.kasania.netp

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class Connection private constructor() {

    companion object{
        val instance = Connection()
    }

    private val TYPE_VERIFICATION = 'V'
    private val TYPE_IMAGE = 'I'
    private val TYPE_SYNCDONE = 'D'

    private lateinit var onSyncSuccess:() -> Int

    private val isSynchronized : AtomicBoolean = AtomicBoolean(false)

    private val executorService : ExecutorService = Executors.newSingleThreadExecutor();
    private val writeExecutor : ExecutorService = Executors.newFixedThreadPool(4);
    private lateinit var address : String

    private var port : Int = 0

    private lateinit var socketChannel :SocketChannel

    fun connect(address: String, port: Int) {
        this.address = address
        this.port = port
        executorService.execute(this::establishConnection)
        executorService.execute { while (isConnected()) read() }
    }

    fun disconnect(){
        executorService.shutdown();
        writeExecutor.shutdown()
        socketChannel.finishConnect()
        socketChannel.close()
    }

    private fun establishConnection(){
        socketChannel = SocketChannel.open()
        socketChannel.connect(InetSocketAddress(address, port))
    }

    fun isConnected() : Boolean{
        return socketChannel.isConnected
    }

    fun sendVerificationCode(code: String){
        writeExecutor.execute {
            val codeBytes = StandardCharsets.UTF_8.encode(code)
            val packagedData = ByteBuffer.allocate(2 + codeBytes.limit())
            packagedData.putChar(TYPE_VERIFICATION)
            packagedData.put(codeBytes)
            packagedData.flip()
            send(packagedData)
        }
    }

    fun sendImage(bitmap: Bitmap){
        if(isSynchronized.get()){

            writeExecutor.execute {
                val baos = ByteArrayOutputStream()
                val matrix = Matrix()
                matrix.postScale(240.toFloat() / bitmap.width, 360.toFloat() / bitmap.height)
                val resized = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        false
                )
                resized.compress(Bitmap.CompressFormat.PNG, 90, baos)
                val bitmapBytes = baos.toByteArray()

                val packagedData = ByteBuffer.allocate(2 + bitmapBytes.size)
                packagedData.putChar(TYPE_IMAGE)
                packagedData.put(bitmapBytes)
                packagedData.flip()
                send(packagedData)
            }
        }

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
        val size = ByteBuffer.allocate(4)
        var packagedData: ByteBuffer? = null

        try {
            socketChannel.read(size)
            size.flip()
            val sz = size.int
            packagedData = ByteBuffer.allocate(sz)
            while (packagedData.hasRemaining()) {
                socketChannel.read(packagedData)
            }
            requireNotNull(packagedData).flip()


            val type = packagedData.char
            val data = ByteArray(packagedData.limit() - 2)
            packagedData.get(data)

            if(type == TYPE_SYNCDONE){
                isSynchronized.set(true)
                onSyncSuccess.invoke()
            }

        } catch (ex: ClosedByInterruptException) {
            //Disconnected
        } catch (e: IOException) {
            e.printStackTrace()
            disconnect()
        }

    }

    fun onSyncSucceed(run: () -> Int) {
        onSyncSuccess = run
    }


}