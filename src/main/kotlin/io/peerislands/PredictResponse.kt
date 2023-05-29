package io.peerislands

import com.google.gson.annotations.SerializedName

data class PredictResponse(
    @SerializedName("predictions")
    val predictions: List<Prediction>
)

data class Prediction(
    @SerializedName("content")
    val content: String,
    @SerializedName("recitationResult")
    val recitationResult: RecitationResult,
    @SerializedName("safetyAttributes")
    val safetyAttributes: SafetyAttributes
)

data class RecitationResult(
    @SerializedName("recitations")
    val recitations: List<Any>, // You can replace 'Any' with the appropriate data class if needed
    @SerializedName("recitationAction")
    val recitationAction: String
)

data class SafetyAttributes(
    @SerializedName("blocked")
    val blocked: Boolean,
    @SerializedName("scores")
    val scores: List<Double>,
    @SerializedName("categories")
    val categories: List<String>
)