package client

import tornadofx.App
import tornadofx.launch

class ClientApp: App(ClientView::class)

fun main(args: Array<String>) {
    launch<ClientApp>(args)
}