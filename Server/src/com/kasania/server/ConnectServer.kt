package com.kasania.server

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.util.*

class ConnectServer {
    private var selector: Selector? = null
    private var pendingSocketChannels: MutableList<SocketChannel>? = null
    private var desktopConnections: MutableList<DesktopConnection>? = null
    private var mobileConnections: MutableList<MobileConnection>? = null

    init {
        pendingSocketChannels = Collections.synchronizedList(ArrayList())
        desktopConnections = Collections.synchronizedList(ArrayList())
        mobileConnections = Collections.synchronizedList(ArrayList())

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

            val selectionKeySet = selector!!.selectedKeys()
            val iterator = selectionKeySet.iterator()

            while (iterator.hasNext()) {
                val selectionKey =  iterator.next() as SelectionKey

                if (selectionKey.isAcceptable) {
                    acceptClient(selectionKey);
                }
                else if (selectionKey.isReadable) {
                    read(selectionKey);
                }

                iterator.remove();
            }
        }
    }

    private fun disconnect(selectedKey: SelectionKey) {

        val socketChannel = selectedKey.channel() as SocketChannel
        pendingSocketChannels!!.remove(socketChannel)

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
            pendingSocketChannels!!.add(socketChannel)
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
            socketChannel.read(packagedData)
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
        desktopConnections!!.add(desktopConnection)
        pendingSocketChannels!!.remove(socketChannel)
        desktopConnection.sendSync()
    }

    private fun mobileVerification(socketChannel: SocketChannel, data: ByteArray){

        val connectionID = String(data)

        desktopConnections!!.forEach {

            if(it.connectionID == connectionID){

                //connection success

                val mobileConnection = MobileConnection(socketChannel, connectionID)
                mobileConnections!!.add(mobileConnection)
                pendingSocketChannels!!.remove(socketChannel)

                it.syncDone()
                mobileConnection.syncDone()


                return
            }

        }

        //fail
    }

    private fun broadCastImage(_unused: SocketChannel, data: ByteArray){
        desktopConnections!!.forEach { it.sendImage(ByteBuffer.wrap(data)) }
    }


}