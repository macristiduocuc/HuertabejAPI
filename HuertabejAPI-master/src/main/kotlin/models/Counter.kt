package com.huertabeja.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class Counter(
    @BsonId val id: String,
    val seq: Int
)