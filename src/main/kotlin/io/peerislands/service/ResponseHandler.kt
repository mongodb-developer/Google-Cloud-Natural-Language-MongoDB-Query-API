package io.peerislands.service

import com.google.gson.Gson
import io.ktor.client.statement.*
import io.peerislands.model.response.PredictResponse

suspend fun parseResponse(response: HttpResponse): String {
    //Parse response to JSONResponse using GSON
    //TODO: Improve error handling and response
    val predictResponse = Gson().fromJson(response.bodyAsText(), PredictResponse::class.java)
    val answer = if (predictResponse.predictions.isNotEmpty())
        predictResponse.predictions[0].content
    else
        "No predictions found"
    return answer.replace("```", "")
}