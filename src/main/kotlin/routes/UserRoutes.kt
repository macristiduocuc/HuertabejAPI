package com.huertabeja.routes

import com.huertabeja.database.DatabaseConnection
import com.huertabeja.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import at.favre.lib.crypto.bcrypt.BCrypt
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.firstOrNull

fun Route.userRouting() {
    route("/auth") {

        post("/register") {
            try {
                val registrationInfo = call.receive<User>()

                if (registrationInfo.email.isBlank() || registrationInfo.passwordHash.length < 8) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Datos de registro incompletos o contraseña muy corta.")
                }

                val existingUser = DatabaseConnection.usersCollection
                    .find(Filters.eq(User::email.name, registrationInfo.email))
                    .firstOrNull()

                if (existingUser != null) {
                    return@post call.respond(HttpStatusCode.Conflict, "El correo electrónico ya está registrado.")
                }

                val hashedPassword = BCrypt.withDefaults().hashToString(12, registrationInfo.passwordHash.toCharArray())

                val newUser = registrationInfo.copy(passwordHash = hashedPassword)

                DatabaseConnection.usersCollection.insertOne(newUser)

                val userResponse = newUser.copy(passwordHash = "HIDDEN")

                call.respond(HttpStatusCode.Created, userResponse)
            } catch (e: Exception) {
                call.respondText("Error al procesar el registro: ${e.localizedMessage}", status = HttpStatusCode.InternalServerError)
            }
        }
        post("/login") {
            try {
                val credentials = call.receive<User>()
                val user = DatabaseConnection.usersCollection
                    .find(Filters.eq(User::email.name, credentials.email))
                    .firstOrNull()

                if (user == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas. Email no encontrado.")
                }
                val result = BCrypt.verifyer().verify(credentials.passwordHash.toCharArray(), user.passwordHash)

                if (result.verified) {
                    val userResponse = user.copy(passwordHash = "HIDDEN")
                    call.respond(HttpStatusCode.OK, userResponse)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas. Contraseña incorrecta.")
                }

            } catch (e: Exception) {
                call.respondText("Error al intentar iniciar sesión: ${e.localizedMessage}", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}