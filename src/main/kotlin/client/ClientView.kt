package client

import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import pojo.Header
import pojo.Message
import tornadofx.View
import java.io.EOFException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

const val HIJACK_ADDRESS = "127.0.0.1"
const val PROXY_ADDRESS = "127.0.0.1"
const val PROXY_PORT = 50005

class ClientView: View() {
    override val root:VBox by fxml()
    private val textField: TextField by fxid()
    private val sendButton: Button by fxid()
    private val textArea: TextArea by fxid()

    init {
        sendButton.setOnAction { sendMessage() }
        textField.setOnAction { sendMessage() }
        val console = Console(textArea)
        System.setOut(PrintStream(console))
    }

    private fun sendMessage(){
        val targetAddress = "127.0.0.1"
        val targetPort = 50000
        val connectAddress = if (targetAddress == HIJACK_ADDRESS) PROXY_ADDRESS else targetAddress
        val connectPort = if (targetAddress == HIJACK_ADDRESS) PROXY_PORT else targetPort

        val header = Header(
            InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), connectPort),
            InetSocketAddress(targetAddress, targetPort)
        )
        val message = Message(header, textField.text)
        Socket(connectAddress, connectPort).use { socket ->
            ObjectOutputStream(socket.getOutputStream()).use { oos ->
                oos.writeObject(message)

                println("Sending message: $message ...")
                try {
                    ObjectInputStream(socket.getInputStream()). use { ois ->
                        val returnMessage = ois.readObject() as Message
                        println("Returned from server: $returnMessage")
                    }
                } catch (e: EOFException) {
                    println("Error: No response from server")
                }
            }
        }
    }
}
