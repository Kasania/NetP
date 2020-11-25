package com.kasania.server

import java.nio.channels.SocketChannel

class MobileConnection(socketChannel: SocketChannel, val connectionID : Int) : Connection(socketChannel, Type.MOBILE) {

}