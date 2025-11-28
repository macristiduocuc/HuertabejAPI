package com.huertabeja.database

import com.huertabeja.models.Product
import com.huertabeja.models.User
import com.mongodb.kotlin.client.coroutine.MongoClient

object DatabaseConnection {
    private const val ATLAS_URI = "mongodb+srv://macristi:macristiduoc@huertabejadb.tm8lmbn.mongodb.net/"

    private const val DATABASE_NAME = "HuertabejaDB"

    private val client = MongoClient.create(ATLAS_URI)

    private val database = client.getDatabase(DATABASE_NAME)

    val productsCollection = database.getCollection<Product>("productos")
    val usersCollection = database.getCollection<User>("usuarios")

    fun checkConnection() {
        println("======================================================")
        println("Conexión a MongoDB Atlas configurada y lista.")
        println("======================================================")
    }
}