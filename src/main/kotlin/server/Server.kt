package server

import pojo.Header
import pojo.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket

fun main(args: Array<String>) {
    val listeningPort = 50000
    ServerSocket(listeningPort).use { serverSocket ->
        println("Server listening on port ${serverSocket.localPort}")
        while (true) {
            serverSocket.accept().use { socket ->
                ObjectInputStream(socket.getInputStream()).use { ois ->
                    val recMessage = ois.readObject() as Message
                    println("Received message: $recMessage")
                    val targetAddress = recMessage.header.clientAddress.hostName

                    val message = Message(
                        Header(
                            InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), socket.localPort),
                            InetSocketAddress(targetAddress, socket.port)
                        ),
                        "Received this message: ${recMessage.body}"
                    )
                    ObjectOutputStream(socket.getOutputStream()).use {
                        it.writeObject(message)
                    }
                }
            }
        }
    }
}