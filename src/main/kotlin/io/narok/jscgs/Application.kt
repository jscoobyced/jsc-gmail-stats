package io.narok.jscgs

import io.ktor.server.application.*
import io.narok.jscgs.plugins.configureRouting
import io.narok.jscgs.plugins.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
