package com.kasania.server

import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.util.*
import kotlin.collections.HashMap

class ConnectServer {
    private var selector: Selector? = null
    private var connections: MutableMap<SocketChannel, Connection> = Collections.synchronizedMap(HashMap())
    private var desktopConnections: MutableMap<SocketChannel, DesktopConnection> = Collections.synchronizedMap(HashMap())
    private var mobileConnections: MutableMap<SocketChannel, MobileConnection> = Collections.synchronizedMap(HashMap())

    init {

        DataType.LOGIN.addReceiver(this::desktopLogin)
        DataType.VERIFY.addReceiver(this::mobileVerification)
        DataType.IMAGE.addReceiver(this::broadCastImage)


        try {
            selector = Selector.open()
            
            val serverSocketChannel = ServerSocketChannel.open()
            serverSocketChannel.bind(InetSocketAddress(11111))
            serverSocketChannel.configureBlocking(false)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() {
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
//                    mobileConnection?.let { connections[mobileConnection.socketChannel]!!.changeType(Connection.Type.PENDING) }


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
            connections[socketChannel] = Connection(socketChannel,Connection.Type.PENDING)
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
            connections[socketChannel]?.changeType(type = Connection.Type.MOBILE)

            desktopConnection.syncDone()
            mobileConnection.syncDone()
            desktopConnection.syncDone.set(true)

            println("sync : $connectionID, $desktopConnection, $mobileConnection")
            sendUserList()
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

    private fun broadCastImage(src: SocketChannel, data: ByteArray){
        val imageData = ByteBuffer.wrap(data)
        for (desktopConnection in desktopConnections) {

            desktopConnection.value.sendImage(imageData, mobileConnections[src]!!.connectionID)
        }
    }


}