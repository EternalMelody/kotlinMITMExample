package client

import javafx.scene.control.TextArea
import java.io.OutputStream

class Console(private val textArea: TextArea):OutputStream(){
    override fun write(b: Int) {
        textArea.appendText(b.toChar().toString())
    }
}