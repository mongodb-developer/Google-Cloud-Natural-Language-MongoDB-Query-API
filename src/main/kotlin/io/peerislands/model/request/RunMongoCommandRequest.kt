package io.peerislands.model.request

data class RunMongoCommandRequest(
    val command: String,
    val db: String
)