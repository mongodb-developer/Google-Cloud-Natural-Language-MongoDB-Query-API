package io.peerislands.service

import io.peerislands.model.response.PredictResponse

fun parseResponse(predictResponse: PredictResponse): String {
    val answer = if (predictResponse.predictions.isNotEmpty())
        predictResponse.predictions[0].content
    else
        "No predictions found"
    return answer.replace("```", "")
}