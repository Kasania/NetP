package com.kasania.server

import java.nio.channels.SocketChannel

class MobileConnection(socketChannel: SocketChannel, val connectionID : String) : Connection(socketChannel, Type.MOBILE) {

}