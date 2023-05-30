package io.peerislands

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import io.peerislands.model.PredictRequest

suspend fun PipelineContext<Unit, ApplicationCall>.predictRequest(): PredictRequest {
    val request = call.receiveText() //TODO: Can we use call.receive<PredictRequest>() instead?
    val requestJson = Gson().fromJson(request, PredictRequest::class.java)
    //TODO: Validate requestJson
    return requestJson
}