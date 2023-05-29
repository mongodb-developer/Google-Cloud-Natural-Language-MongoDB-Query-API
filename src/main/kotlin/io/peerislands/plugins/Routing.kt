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
import io.peerislands.PredictRequest

import io.peerislands.PredictResponse


fun Application.configureRouting() {
    //TODO: Get endpoint from config
    val endpoint =
        "https://us-central1-aiplatform.googleapis.com/v1/projects/peer-poc/locations/us-central1/publishers/google/models/code-bison:predict"
    routing {
        post("/predict") {
            val request = call.receiveText() //TODO: Can we use call.receive<PredictRequest>() instead?
            //Convert request to JSON using GSON
            val jsonRequest: PredictRequest = Gson().fromJson(request, PredictRequest::class.java)
            println("Request: $jsonRequest")

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
                setBody(    //TODO: Can we extract this to a function?
                    """
                    {
                        "instances": [
                            {
                            "prefix": "Write a MongoDB update statement. Replace the “Completed” inspection result to use only “No Violation Issued” for those inspections. Update all the cases accordingly. Use the following schema for inspections collection. _id bson.objectid.ObjectId id str certificate_number int business_name str date str result str sector str address dict",
                            "suffix": ""
                            }
                        ],
                        "parameters": {
                            "task": "GENERATION",
                            "temperature": 0.2,
                            "maxOutputTokens": 512,
                            "candidateCount": 1,
                        }
                    }
                    """.trimIndent()
                )
            }

            //Parse response to JSONResponse using GSON
            //TODO: Improve error handling and response
            val predictResponse = Gson().fromJson(response.bodyAsText(), PredictResponse::class.java)
            if (predictResponse.predictions.isNotEmpty())
                call.respondText(predictResponse.predictions[0].content, ContentType.Text.Plain)
            else
                call.respondText("No predictions found", ContentType.Text.Plain)

        }
    }
}

