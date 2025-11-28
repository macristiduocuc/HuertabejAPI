package com.huertabeja

import com.huertabeja.database.DatabaseConnection
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseConnection.checkConnection()
    configureSerialization()
    configureRouting()
}
