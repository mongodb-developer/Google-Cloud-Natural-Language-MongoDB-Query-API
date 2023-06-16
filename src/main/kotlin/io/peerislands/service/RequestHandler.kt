package io.peerislands.service

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import io.peerislands.model.request.PredictRequest

suspend fun PipelineContext<Unit, ApplicationCall>.predictRequest(): PredictRequest {
    return call.receive<PredictRequest>()
}