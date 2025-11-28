package com.huertabeja.database

import com.huertabeja.models.Product
import com.huertabeja.models.User
import com.huertabeja.models.Counter // <-- NUEVO IMPORT
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates

object DatabaseConnection {
    private const val ATLAS_URI = "mongodb+srv://macristi:macristiduoc@huertabejadb.tm8lmbn.mongodb.net/"

    private const val DATABASE_NAME = "HuertabejaDB"

    private val client = MongoClient.create(ATLAS_URI)

    private val database = client.getDatabase(DATABASE_NAME)

    val productsCollection = database.getCollection<Product>("productos")
    val usersCollection = database.getCollection<User>("usuarios")

    // NUEVA COLECCIÓN: Para guardar el contador
    private val counterCollection = database.getCollection<Counter>("counters")

    // FUNCIÓN CRUCIAL: Obtiene el siguiente ID de forma atómica
    suspend fun getNextSequence(collectionName: String): Int {
        // Incrementa el campo 'seq' en 1
        val update = Updates.inc(Counter::seq.name, 1)

        // Opciones: devuelve el documento *después* de la actualización
        val options = com.mongodb.client.model.FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)

        // Ejecuta la operación atómica: busca el contador y lo incrementa
        val counter = counterCollection.findOneAndUpdate(
            Filters.eq(Counter::id.name, collectionName),
            update,
            options
        )

        // Devuelve el nuevo valor. Si el contador no existe, lanza un error.
        return counter?.seq ?: throw Exception("Contador para la colección $collectionName no encontrado. Debe ser inicializado en Atlas.")
    }

    fun checkConnection() {
        println("======================================================")
        println("Conexión a MongoDB Atlas configurada y lista para usar")
        println("======================================================")
    }
}