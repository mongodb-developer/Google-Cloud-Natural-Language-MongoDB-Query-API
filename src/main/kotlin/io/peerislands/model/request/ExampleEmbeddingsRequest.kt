package io.peerislands.model.request

data class ExampleEmbeddingsRequest (
    val operation: String,
    val example: String,
    val keywords: List<String>
)