package io.narok.jscgs

import io.ktor.server.application.*
import io.narok.jscgs.plugins.configureRouting
import io.narok.jscgs.plugins.configureSerialization
import io.narok.jscgs.repository.UserRepository
import io.narok.jscgs.repository.createDatabase

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
    createDatabase()
    UserRepository.showUsers()
}
