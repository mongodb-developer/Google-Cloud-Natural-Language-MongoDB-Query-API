package io.peerislands.model

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val collectionName: String,
    val collectionSchema: String,
    val collectionKeywords: List<String>
)