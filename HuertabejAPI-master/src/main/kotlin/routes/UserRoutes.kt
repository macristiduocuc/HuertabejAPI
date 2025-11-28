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
                // 1. DEFINICIÓN: Aquí se define la variable registrationInfo
                val registrationInfo = call.receive<User>()

                // 2. Validación de campos mínimos
                if (registrationInfo.email.isBlank() || registrationInfo.passwordHash.length < 8) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Datos de registro incompletos o contraseña muy corta.")
                }

                // 3. Verificar si el usuario ya existe (usando registrationInfo)
                val existingUser = DatabaseConnection.usersCollection
                    .find(Filters.eq(User::email.name, registrationInfo.email))
                    .firstOrNull()

                if (existingUser != null) {
                    return@post call.respond(HttpStatusCode.Conflict, "El correo electrónico ya está registrado.")
                }

                // 4. Lógica de Hashing y Contador ID (Usando registrationInfo)
                val hashedPassword = BCrypt.withDefaults().hashToString(12, registrationInfo.passwordHash.toCharArray())

                // --- Lógica del ID Secuencial ---
                val nextId = DatabaseConnection.getNextSequence("usuarios")

                val newUser = registrationInfo.copy(
                    id = nextId, // ASIGNAMOS EL ID NUMÉRICO
                    passwordHash = hashedPassword
                )
                // ---------------------------------

                // 5. Inserción
                DatabaseConnection.usersCollection.insertOne(newUser)

                // 6. Respuesta
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