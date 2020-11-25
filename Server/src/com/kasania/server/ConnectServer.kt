package com.kasania.server

import com.kasania.server.connection.Connection
import com.kasania.server.connection.DesktopConnection
import com.kasania.server.connection.MobileConnection
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.util.*
import kotlin.collections.HashMap

class ConnectServer {
    private var selector: Selector? = null
    private var imageSelector: Selector? = null
    private var audioSelector: Selector? = null
    private var connections: MutableMap<SocketChannel, Connection> = Collections.synchronizedMap(HashMap())
    private var desktopConnections: MutableMap<SocketChannel, DesktopConnection> = Collections.synchronizedMap(HashMap())
    private var mobileConnections: MutableMap<SocketChannel, MobileConnection> = Collections.synchronizedMap(HashMap())

    init {

        DataType.LOGIN.addReceiver(this::desktopLogin)
        DataType.VERIFY.addReceiver(this::mobileVerification)
        DataType.TEXT.addReceiver(this::broadCastText)

        try {
            selector = Selector.open()
            imageSelector = Selector.open()
            audioSelector = Selector.open()

            val serverSocketChannel = ServerSocketChannel.open()
            serverSocketChannel.bind(InetSocketAddress(11111))
            serverSocketChannel.configureBlocking(false)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

            val imageChannel = DatagramChannel.open()
            imageChannel.bind(InetSocketAddress(11112))
            imageChannel.configureBlocking(false)
            imageChannel.register(imageSelector, SelectionKey.OP_READ)


            val audioChannel = DatagramChannel.open()
            audioChannel.bind(InetSocketAddress(11113))
            audioChannel.configureBlocking(false)
            audioChannel.register(audioSelector, SelectionKey.OP_READ)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() {
        Thread{
            while (true) {
                imageSelector!!.select(); //select() 메소드로 준비된 이벤트가 있는지 확인한다.
                for (selectedKey in imageSelector!!.selectedKeys()) {
                    try{
                        if (selectedKey.isReadable) {
                            readImage(selectedKey)
                        }
                    }catch (exp: Exception){
                    }
                }
                imageSelector!!.selectedKeys().clear()
            }
        }.start()


        Thread{
            while (true) {
                audioSelector!!.select(); //select() 메소드로 준비된 이벤트가 있는지 확인한다.

                for (selectedKey in audioSelector!!.selectedKeys()) {
                    try{
                        if (selectedKey.isReadable) {
                            readAudio(selectedKey)
                        }
                    }catch (exp: Exception){
                    }
                }
                audioSelector!!.selectedKeys().clear()

            }
        }.start()

        Thread{
            while (true) {
                selector!!.select(); //select() 메소드로 준비된 이벤트가 있는지 확인한다.

                for (selectedKey in selector!!.selectedKeys()) {
                    try{

                        if (selectedKey.isAcceptable) {
                            acceptClient(selectedKey);
                        }
                        else if (selectedKey.isReadable) {
                            read(selectedKey);
                        }
                    }catch (exp: Exception){
                        try {
                            disconnect(selectedKey)
                        }catch (exp:Exception){}

                    }

                }
                selector!!.selectedKeys().clear()

            }
        }.start()
    }

    private fun disconnect(selectedKey: SelectionKey) {

        val socketChannel = selectedKey.channel() as SocketChannel
        println("disconnect : $socketChannel")
        val connection = connections[socketChannel]

        if (connection != null) {
            when(connection.getType()){
                Connection.Type.PENDING -> connections.remove(socketChannel)
                Connection.Type.MOBILE -> {
                    val mobileConnection = mobileConnections.remove(socketChannel)
                    connections.remove(socketChannel)
                    // synchronized desktop connection to pending
                    val desktopConnection = findDesktopByConnectionID(mobileConnection!!.connectionID)
                    desktopConnection?.let {
                        desktopConnection.syncDone.set(true)
                        desktopConnection.sendSync()
                    }
                }
                Connection.Type.DESKTOP -> {
                    val desktopConnection = desktopConnections.remove(socketChannel)
                    connections.remove(socketChannel)
                    // synchronized mobile connection to pending
                    val mobileConnection = findMobileByConnectionID(desktopConnection!!.connectionID)
                    //do mobile waiting
                    mobileConnection?.let { mobileConnection.syncCancel() }


                }
            }
        }
        socketChannel.shutdownOutput()
        socketChannel.shutdownInput()
        socketChannel.close()

    }

    @Throws(IOException::class)
    private fun acceptClient(selectionKey: SelectionKey) {
        val socketChannel = (selectionKey.channel() as ServerSocketChannel).accept()
        if (Objects.nonNull(socketChannel)) {
            socketChannel.configureBlocking(false)
            socketChannel.register(selector, SelectionKey.OP_READ)
            connections[socketChannel] = Connection(socketChannel, Connection.Type.PENDING)
        }
        println("accepted : $selectionKey")
    }

    @Throws(IOException::class)
    private fun read(selectionKey: SelectionKey) {
        val socketChannel = selectionKey.channel() as SocketChannel

        val size = ByteBuffer.allocate(Integer.BYTES)
        val packagedData: ByteBuffer
        socketChannel.read(size)
        size.flip()
        val sz = size.int
        packagedData = ByteBuffer.allocate(sz)
        while (packagedData.hasRemaining()) {
            synchronized(socketChannel){
                if (socketChannel.isConnected)
                    socketChannel.read(packagedData)
            }
        }
        packagedData.flip()
        val type = packagedData.char
        val data = ByteArray(packagedData.limit() - Character.BYTES)
        packagedData.get(data)

        DataType.getType(type).received(socketChannel, data)

    }

    @Throws(IOException::class)
    private fun readImage(selectionKey: SelectionKey) {
        val datagramChannel = selectionKey.channel() as DatagramChannel
        val packagedData: ByteBuffer = ByteBuffer.allocate(16384)
        datagramChannel.receive(packagedData)
        packagedData.flip()
        broadCastImage(0,packagedData)
    }

    @Throws(IOException::class)
    private fun readAudio(selectionKey: SelectionKey) {
        val datagramChannel = selectionKey.channel() as DatagramChannel
        val packagedData: ByteBuffer = ByteBuffer.allocate(3528 + Int.SIZE_BYTES)
        datagramChannel.receive(packagedData)
        packagedData.flip()
        broadCastAudio(0,packagedData)
    }




    private fun desktopLogin(socketChannel: SocketChannel, data: ByteArray){
        println("login : $socketChannel")
        val desktopConnection = DesktopConnection(socketChannel, String(data, Charsets.UTF_8))
        desktopConnections[socketChannel] = desktopConnection
        connections[socketChannel]?.changeType(type = Connection.Type.DESKTOP)

        desktopConnection.sendSync()
    }

    private fun mobileVerification(socketChannel: SocketChannel, data: ByteArray){

        val connectionID = ByteBuffer.wrap(data).int
        val desktopConnection = findDesktopByConnectionID(connectionID)
        if(desktopConnection != null){

            val mobileConnection = MobileConnection(socketChannel, connectionID)

            mobileConnections[socketChannel] = mobileConnection
            connections[socketChannel]?.changeType(Connection.Type.MOBILE)

            desktopConnection.syncDone()
            mobileConnection.syncDone()
            desktopConnection.syncDone.set(true)

            println("sync : $connectionID, ${desktopConnection.socketChannel}, ${mobileConnection.socketChannel}")
            sendUserList()
        }

    }

    private fun sendUserList(){
        val connectedUsers = StringBuilder()
        for (desktopConnection in desktopConnections) {
            val idToken = "${desktopConnection.value.connectionID}::${desktopConnection.value.name}//"
            connectedUsers.append(idToken)
        }

        desktopConnections.forEach {
            it.value.sendUserList(connectedUsers.toString())
        }
    }

    private fun broadCastImage(src: Int, data: ByteBuffer){
        for (desktopConnection in desktopConnections) {
            desktopConnection.value.sendImage(data,0)
        }
    }

    private fun broadCastAudio(src: Int, data: ByteBuffer){
        for (desktopConnection in desktopConnections) {
            desktopConnection.value.sendAudio(data,0)
        }
    }

    private fun broadCastText(socketChannel: SocketChannel, data: ByteArray){
        for (desktopConnection in desktopConnections) {
            desktopConnection.value.send(DataType.TEXT, desktopConnections[socketChannel]!!.connectionID,ByteBuffer.wrap(data))
        }
    }


    private fun findDesktopByConnectionID(connectionID:Int ) : DesktopConnection? {
        for (desktopConnection in desktopConnections) {
            if(desktopConnection.value.connectionID == connectionID){
                return desktopConnection.value
            }
        }
        return null
    }

    private fun findMobileByConnectionID(connectionID:Int ) : MobileConnection? {
        for (mobileConnection in mobileConnections) {
            if(mobileConnection.value.connectionID == connectionID){
                return mobileConnection.value
            }
        }
        return null
    }

    private fun debug(socketChannel: SocketChannel, data: ByteArray){
        println("$socketChannel:$data")
    }
}