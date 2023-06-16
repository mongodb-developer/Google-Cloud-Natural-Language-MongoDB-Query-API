package io.peerislands.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ExampleEmbeddingsRequest (
    val operation: String,
    val example: String,
    val keywords: List<String>
)