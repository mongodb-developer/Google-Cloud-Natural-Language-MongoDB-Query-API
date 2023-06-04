package io.peerislands.model

data class ExampleEmbeddingsRequest (
    val operation: String,
    val example: String,
    val keywords: List<String>
)