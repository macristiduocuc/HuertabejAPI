package com.huertabeja.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class User(
    @BsonId
    val id: String = ObjectId().toHexString(),
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String
)