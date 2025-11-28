package com.huertabeja

import com.huertabeja.routes.productRouting
import com.huertabeja.routes.userRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Huertabeja API está funcionando!")
        }
        productRouting()

        userRouting()
    }
}
