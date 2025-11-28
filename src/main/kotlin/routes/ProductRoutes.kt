package com.huertabeja.routes

import com.huertabeja.database.DatabaseConnection
import com.huertabeja.models.Product
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId

fun Route.productRouting() {
    route("/products") {
        get {
            try {
                val products = DatabaseConnection.productsCollection.find().toList()
                call.respond(products)
            } catch (e: Exception) {
                call.respondText("Error al obtener productos: ${e.localizedMessage}", status = HttpStatusCode.InternalServerError)
            }
        }
        post {
            try {
                val product = call.receive<Product>()
                val result = DatabaseConnection.productsCollection.insertOne(product)
                if (result.wasAcknowledged()) {
                    call.respond(HttpStatusCode.Created, product)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Error desconocido al guardar el producto")
                }
            } catch (e: Exception) {
                call.respondText("Solicitud inválida o error: ${e.localizedMessage}", status = HttpStatusCode.BadRequest)
            }
        }
        delete("/{id}") {
            val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "ID de producto faltante")
            try {
                val objectId = ObjectId(idParam)
                val filter = Document("_id", objectId)
                val result = DatabaseConnection.productsCollection.deleteOne(filter)
                if (result.deletedCount == 1L) {
                    call.respond(HttpStatusCode.OK, "Producto eliminado exitosamente")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Producto con ID $idParam no encontrado")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ID inválido: debe ser un formato ObjectId de 24 caracteres")
            } catch (e: Exception) {
                call.respondText("Error al eliminar el producto: ${e.localizedMessage}", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}