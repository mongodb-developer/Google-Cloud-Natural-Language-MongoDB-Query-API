package io.peerislands.model

data class EmbeddingsRequest (
    val collectionName: String,
    val schema: String,
    val keywords: List<String>
)