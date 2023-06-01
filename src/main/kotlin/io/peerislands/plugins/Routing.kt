package io.peerislands.plugins

import com.google.gson.Gson
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.peerislands.*
import io.peerislands.model.PredictRequest


fun Application.configureRouting() {
    routing {
        post("/api/v1/predict") {
            //STEP 1: Get question from request
            val jsonRequest: PredictRequest = predictRequest()

            //STEP 2: Construct Prompt
            val prompt = constructPayload(jsonRequest)

            //STEP 3: Call GEN AI code-bison endpoint
            val generatedCode: HttpResponse = callGenAI(prompt)
//
//            //STEP 4: Parse response and get answer / code
            val parsedCode: String = parseResponse(generatedCode)
            println("parsedCode: $parsedCode")

            //STEP 5: Run validations
            val isValid: Boolean = validateResponse(parsedCode)

            //STEP 6: Regenerate code if any errors
            if (!isValid) {
                print("Invalid response. Regenerating code...")
            } else {
                print("Valid response. Returning code...")
            }

            //STEP 7: Store question, prompt, and response in MongoDB
            storeInMongoDB(jsonRequest, prompt, parsedCode)

            //STEP 8: Return response
            val response = buildResponse(parsedCode, prompt)
            call.respondText(response, ContentType.Application.Json)
        }
        get("/api/v1/history") {
            val limit = call.parameters["limit"]?.toInt() ?: 10
            val response = getHistory(limit)
            call.respondText(response, ContentType.Application.Json)
        }
    }
}


