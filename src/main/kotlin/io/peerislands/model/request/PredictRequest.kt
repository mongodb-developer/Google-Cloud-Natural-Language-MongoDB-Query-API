package io.peerislands.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PredictRequest(
    val instances: List<Instance>,
    val parameters: Parameters
)
@Serializable
data class Instance(
    val prefix: String,
    val suffix: String = "",
    val context: String = "",
    val examples: String = ""
)
@Serializable
data class Parameters(
    val task: String = "GENERATION",
    val temperature: Double = 0.3,
    val maxOutputTokens: Int = 512,
    val candidateCount: Int = 1
)