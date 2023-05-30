package io.peerislands.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.peerislands.PredictRequest

import io.peerislands.PredictResponse
import io.peerislands.endpoint


fun Application.configureRouting() {
    routing {
        post("/api/v1/predict") {
            //STEP 1: Get question from request
            val jsonRequest: PredictRequest = predictRequest()

            //STEP 2: Construct Prompt
            val prompt = constructPrompt(jsonRequest)

            //STEP 3: Call GEN AI code-bison endpoint
            val response: HttpResponse = callGenAI(prompt)

            //STEP 4: Parse response and get answer / code
            val answer: String = parseResponse(response)

            //STEP 5: Run validations
            //          - Check for syntax errors
            //          - Check for semantic errors - field names, data types, etc.
            //STEP 6: Regenerate code if any errors

            //STEP 7: Return response
            call.respondText(answer, ContentType.Text.Plain)

        }
    }
}

private suspend fun parseResponse(response: HttpResponse): String {
    //Parse response to JSONResponse using GSON
    //TODO: Improve error handling and response
    val predictResponse = Gson().fromJson(response.bodyAsText(), PredictResponse::class.java)
    val answer: String
    if (predictResponse.predictions.isNotEmpty()) {
        answer = predictResponse.predictions[0].content
    } else {
        answer = "No predictions found"
    }
    return answer
}

private suspend fun callGenAI(prompt: String): HttpResponse {
    val credentials: GoogleCredentials = GoogleCredentials.getApplicationDefault()
    val token = credentials.refreshAccessToken() //TODO: Is this the right way to get the token?

    //REST API Call
    val client = HttpClient() {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }
    val response: HttpResponse = client.request(endpoint) {
        method = HttpMethod.Post
        headers {
            append("Authorization", "Bearer ${token.tokenValue}")
            append("Content-Type", "application/json")
        }
        setBody(prompt)
    }
    return response
}

fun constructPrompt(jsonRequest: PredictRequest): String {
    //          - Infer collection from question
    //          - Infer question type from question
    //          - Update Prompt template with question, schema and relevant examples
    return """
                    {
                        "instances": [
                            {
                            "prefix": "${jsonRequest.instances[0].prefix}",
                            "suffix": "${jsonRequest.instances[0].suffix}"
                            }
                        ],
                        "parameters": {
                            "task": "GENERATION",
                            "temperature": 0.3,
                            "maxOutputTokens": 256,
                            "candidateCount": 1,
                        }
                    }
                    """.trimIndent()
}

private suspend fun PipelineContext<Unit, ApplicationCall>.predictRequest(): PredictRequest {
    val request = call.receiveText() //TODO: Can we use call.receive<PredictRequest>() instead?
    return Gson().fromJson(request, PredictRequest::class.java)
}

