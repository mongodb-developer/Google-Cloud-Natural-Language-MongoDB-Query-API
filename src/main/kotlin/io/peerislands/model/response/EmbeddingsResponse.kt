package io.peerislands.model.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingsResponse(
    @SerializedName("predictions")
    val predictions: List<EmbeddingsPrediction>
)

@Serializable
data class EmbeddingsPrediction(
    @SerializedName("embeddings")
    val embeddings: Embeddings
)

@Serializable
data class Embeddings(
    @SerializedName("statistics")
    val statistics: Statistics,
    @SerializedName("values")
    val values: List<Double>
)

@Serializable
data class Statistics(
    @SerializedName("truncated")
    val truncated: Boolean,
    @SerializedName("token_count")
    val tokenCount: Int
)