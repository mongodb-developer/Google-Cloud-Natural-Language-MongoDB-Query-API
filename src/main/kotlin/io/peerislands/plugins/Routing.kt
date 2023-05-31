package io.peerislands.plugins

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
            val response: HttpResponse = callGenAI(prompt)
//
//            //STEP 4: Parse response and get answer / code
            val answer: String = parseResponse(response)
            val trimmedAnswer = answer.replace("```", "")

            //STEP 5: Run validations
            val isValid: Boolean = validateResponse(trimmedAnswer)

            //STEP 6: Regenerate code if any errors
            if (!isValid) {
                print("Invalid response. Regenerating code...")
            } else {
                print("Valid response. Returning code...")
            }

            //STEP 7: Return response
            call.respondText(trimmedAnswer, ContentType.Text.Plain)

        }
    }
}



