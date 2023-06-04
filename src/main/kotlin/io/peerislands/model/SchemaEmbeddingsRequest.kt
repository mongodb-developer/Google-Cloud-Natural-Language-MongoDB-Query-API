package io.peerislands.model

data class SchemaEmbeddingsRequest (
    val collectionName: String,
    val schema: String,
    val keywords: List<String>
)