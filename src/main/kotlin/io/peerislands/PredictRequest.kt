package io.peerislands

data class PredictRequest(
    val instances: List<Instance>,
    val parameters: Parameters
)
data class Instance(
    val prefix: String,
    val suffix: String
)
data class Parameters(
    val task: String,
    val temperature: Double,
    val maxOutputTokens: Int,
    val candidateCount: Int
)