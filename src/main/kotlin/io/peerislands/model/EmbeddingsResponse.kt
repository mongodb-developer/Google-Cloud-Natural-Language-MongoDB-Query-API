package io.peerislands.model

import com.google.gson.annotations.SerializedName

data class EmbeddingsResponse(
    @SerializedName("predictions")
    val predictions: List<EmbeddingsPrediction>
)

data class EmbeddingsPrediction(
    @SerializedName("embeddings")
    val embeddings: Embeddings
)

data class Embeddings(
    @SerializedName("statistics")
    val statistics: Statistics,
    @SerializedName("values")
    val values: List<Double>
)

data class Statistics(
    @SerializedName("truncated")
    val truncated: Boolean,
    @SerializedName("token_count")
    val tokenCount: Int
)