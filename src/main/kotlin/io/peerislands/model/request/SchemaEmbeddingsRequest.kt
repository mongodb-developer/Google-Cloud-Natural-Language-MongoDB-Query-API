package io.peerislands.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SchemaEmbeddingsRequest (
    val collectionName: String,
    val schema: String,
    val keywords: List<String>
)