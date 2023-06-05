package io.peerislands.model.request

data class SchemaEmbeddingsRequest (
    val collectionName: String,
    val schema: String,
    val keywords: List<String>
)