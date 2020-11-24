package com.kasania.netp

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs


class Connection private constructor() {

    companion object{
        val instance = Connection()
    }

    private val TYPE_VERIFICATION = 'V'
    private val TYPE_IMAGE = 'I'
    private val TYPE_SYNCDONE = 'D'
    private val TYPE_AUDIO = 'A'

    private lateinit var onSyncSuccess:() -> Int

    private val isSynchronized : AtomicBoolean = AtomicBoolean(false)

    private val executorService : ExecutorService = Executors.newSingleThreadExecutor();
    private val writeExecutor : ExecutorService = Executors.newFixedThreadPool(4);
    private lateinit var address : String

    private var port : Int = 0

    private lateinit var socketChannel :SocketChannel
    private lateinit var dataChannel : DatagramChannel
    private val imageDataPort = 11112
    private val audioDataPort = 11113

    private lateinit var imageDataAddress: InetSocketAddress
    private lateinit var audioDataAddress: InetSocketAddress

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

        dataChannel = DatagramChannel.open()
        imageDataAddress = InetSocketAddress(address,imageDataPort)
        audioDataAddress = InetSocketAddress(address,audioDataPort)
    }

    fun isConnected() : Boolean{
        return socketChannel.isConnected
    }

    fun sendVerificationCode(code: Int){
        writeExecutor.execute {
            val packagedData = ByteBuffer.allocate(2 + 4)
            packagedData.putChar(TYPE_VERIFICATION)
            packagedData.putInt(code)
            packagedData.flip()
            send(packagedData)
        }
    }


    fun sendAudio(data:ByteArray){
        writeExecutor.execute {
//                val packagedData = ByteBuffer.allocate(2 + data.size)
//                packagedData.putChar(TYPE_AUDIO)
//                packagedData.put(data)
//                packagedData.flip()
//                send(packagedData)
            if(isSynchronized.get()){
//
//                val packaged = ByteBuffer.allocate(data.size*2)
//                for (datum in data) {
//                    packaged.putShort(datum)
//                }
//
//                for (index in data.indices) {
//                    if(abs(data[index].toInt())<300){
//                        data[index] = 0
//                    }
//                }

                dataChannel.send(ByteBuffer.wrap(data),audioDataAddress)
            }
            return@execute
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
                resized.compress(Bitmap.CompressFormat.JPEG, 30, baos)
                val bitmapBytes = baos.toByteArray()
                dataChannel.send(ByteBuffer.wrap(bitmapBytes),imageDataAddress)
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
            val src = packagedData.int
            val data = ByteArray(packagedData.limit() - 2 - 4)
            packagedData.get(data)

            if(type == TYPE_SYNCDONE){
                isSynchronized.set(true)
                onSyncSuccess.invoke()
            }

        } catch (ex: ClosedByInterruptException) {
            //Disconnected
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
        }

    }

    fun onSyncSucceed(run: () -> Int) {
        onSyncSuccess = run
    }


}