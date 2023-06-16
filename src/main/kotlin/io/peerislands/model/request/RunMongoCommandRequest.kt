package io.peerislands.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RunMongoCommandRequest(
    val command: String,
    val db: String
)