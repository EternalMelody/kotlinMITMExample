package proxy

import pojo.Header
import pojo.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

const val LISTENING_PORT = 50005

enum class Decision {
    NOMODIFY,
    IGNORE,
    MODIFY
}

fun main(args: Array<String>) {
    ServerSocket(LISTENING_PORT).use {
        println("Proxy listening on port $LISTENING_PORT")
        while (true) {
            it.accept().use loop@ { socketFromClient ->
                ObjectInputStream(socketFromClient.getInputStream()).use { sfcois->
                    val messageFromClient = sfcois.readObject() as Message

                    println("Received from client: $messageFromClient")

                    val serverAddress = messageFromClient.header.serverAddress
                    val clientAddress = messageFromClient.header.clientAddress

                    val decisionToServer = decide()
                    println("Let's ${decisionToServer.name} this message from the CLIENT")
                    if (decisionToServer == Decision.IGNORE) {
                        return@loop
                    }

                    val messageToServerBody = if (decisionToServer == Decision.MODIFY) messageFromClient.body.reversed() else messageFromClient.body
                    val messageToServer = Message(
                        Header(
                            clientAddress,
                            serverAddress
                        ),
                        messageToServerBody
                    )

                    println("Sending $messageToServer")

                    val messageFromServer = Socket(serverAddress.hostName, serverAddress.port).use socketToServer@ {
                        ObjectOutputStream(it.getOutputStream()).use{ stsoos ->
                            stsoos.writeObject(messageToServer)
                            ObjectInputStream(it.getInputStream()).use { stsois ->
                                return@socketToServer stsois.readObject() as Message
                            }
                        }
                    }
                    println("Received from server: \n$messageFromServer")

                    val decisionToClient = decide()
                    println("Let's ${decisionToClient.name} this message from the SERVER")
                    if (decisionToClient == Decision.IGNORE) {
                        return@loop
                    }

                    val messageToClientBody = if (decisionToClient == Decision.MODIFY) messageFromServer.body.reversed() else messageFromServer.body
                    val messageToClient = Message(
                        Header(
                            messageFromClient.header.serverAddress,
                            messageFromClient.header.clientAddress
                        ),
                        messageToClientBody
                    )
                    println("Sending $messageToClient")

                    ObjectOutputStream(socketFromClient.getOutputStream()).use{ sfcoos ->
                        sfcoos.writeObject(messageToClient)
                    }
                }
            }
        }
    }
}

fun decide():Decision {
    return when (Random.nextInt(3)) {
        0 -> Decision.NOMODIFY
        1 -> Decision.IGNORE
        else -> Decision.MODIFY
    }
}