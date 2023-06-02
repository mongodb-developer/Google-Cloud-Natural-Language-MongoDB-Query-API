package io.peerislands.model

data class Context(
    val collectionName: String,
    val collectionSchema: String,
    val collectionKeywords: List<String>
)