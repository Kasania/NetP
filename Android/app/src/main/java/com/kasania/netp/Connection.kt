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


class Connection private constructor() {

    companion object{
        val instance = Connection()
    }

    private val TYPE_VERIFICATION = 'V'
    private val TYPE_SYNCDONE = 'D'
    private val TYPE_SYNCCANCLE = 'C'

    private lateinit var onSyncSuccess:() -> Int

    private lateinit var onSyncCancel:() -> Int

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

    private var connectionID:Int = 0

    fun connect(address: String, port: Int) {
        this.address = address
        this.port = port
        executorService.execute(this::establishConnection)
        executorService.execute { while (isConnected()) read() }
    }

    fun disconnect(){
        isSynchronized.set(false)
        executorService.shutdown();
        writeExecutor.shutdown()
        socketChannel.finishConnect()
        socketChannel.close()
        dataChannel.close()
        onSyncCancel.invoke()
    }

    private fun establishConnection(){
        socketChannel = SocketChannel.open()
        socketChannel.connect(InetSocketAddress(address, port))
        socketChannel.configureBlocking(true)

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
            connectionID = code
        }
    }


    fun sendAudio(data:ByteArray){
        if(isSynchronized.get()){
            writeExecutor.execute {
                val packagedData = ByteBuffer.allocate(4 + data.size)
                packagedData.putInt(connectionID)
                packagedData.put(data)
                packagedData.flip()
                dataChannel.send(packagedData,audioDataAddress)

                return@execute
            }
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
                resized.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                val bitmapBytes = baos.toByteArray()

                val packagedData = ByteBuffer.allocate(4 + bitmapBytes.size)
                packagedData.putInt(connectionID)
                packagedData.put(bitmapBytes)
                packagedData.flip()
                dataChannel.send(packagedData,imageDataAddress)
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


            if(type == TYPE_SYNCDONE){
                val audioPort = packagedData.int
                Log.d("TAG", "limit: ${packagedData.limit()}")
                Log.d("TAG", "src: $src")
                audioDataAddress = InetSocketAddress(address, audioPort)

                isSynchronized.set(true)
                onSyncSuccess.invoke()
            }else if(type == TYPE_SYNCCANCLE) {
                isSynchronized.set(false)
                onSyncCancel.invoke()
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

    fun onSyncCanceled(run: () -> Int) {
        onSyncCancel = run
    }


}