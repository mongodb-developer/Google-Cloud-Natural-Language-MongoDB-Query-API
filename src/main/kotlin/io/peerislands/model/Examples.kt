package io.peerislands.model

import kotlinx.serialization.Serializable

@Serializable
data class Examples (
    val examples: List<Example>
)

@Serializable
data class Example(
    val operation: String,
    val example: String,
    val exampleKeywords: List<String>
)