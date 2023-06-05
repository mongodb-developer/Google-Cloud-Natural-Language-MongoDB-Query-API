package io.peerislands.model.request

data class PredictRequest(
    val instances: List<Instance>,
    val parameters: Parameters
)
data class Instance(
    val prefix: String,
    val suffix: String = "",
    val context: String = "",
    val examples: String = ""
)
data class Parameters(
    val task: String = "GENERATION",
    val temperature: Double = 0.3,
    val maxOutputTokens: Int = 512,
    val candidateCount: Int = 1
)