package io.peerislands.model

data class Examples (
    val examples: List<Example>
)

data class Example(
    val operation: String,
    val example: String,
    val exampleKeywords: List<String>
)