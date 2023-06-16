package io.peerislands.model.response

import com.google.gson.annotations.SerializedName
import io.peerislands.GSON
import kotlinx.serialization.Serializable

@Serializable
data class PredictResponse(
    @SerializedName("predictions")
    val predictions: List<Prediction>
) {
    override fun toString(): String {
        return GSON.toJson(this)
    }
}

@Serializable
data class Prediction(
    @SerializedName("content")
    val content: String,
    @SerializedName("recitationResult")
    val recitationResult: RecitationResult,
    @SerializedName("safetyAttributes")
    val safetyAttributes: SafetyAttributes
)

@Serializable
data class RecitationResult(
    @SerializedName("recitations")
    val recitations: List<String>, // You can replace 'Any' with the appropriate data class if needed
    @SerializedName("recitationAction")
    val recitationAction: String
)

@Serializable
data class SafetyAttributes(
    @SerializedName("blocked")
    val blocked: Boolean,
    @SerializedName("scores")
    val scores: List<Double>,
    @SerializedName("categories")
    val categories: List<String>
)