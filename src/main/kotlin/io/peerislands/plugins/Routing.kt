package io.peerislands.plugins

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.peerislands.GSON
import io.peerislands.logger
import io.peerislands.model.PredictRequest
import io.peerislands.service.*


fun Application.configureRouting() {
    routing {
        post("/api/v1/predict") {
            //STEP 1: Get question from request
            val jsonRequest: PredictRequest = predictRequest()

            //STEP 2: Construct Prompt
            val prompt = constructPayload(jsonRequest)

            //STEP 3: Call GEN AI code-bison endpoint
            val generatedCode: HttpResponse = callGenAIPredict(prompt)

            //STEP 4: Parse response and get answer / code
            val parsedCode: String = parseResponse(generatedCode)
            logger.info { "parsedCode: $parsedCode" }

            //STEP 5: Run validations
            val (validSyntax, validSemantics) = validateResponse(parsedCode)

            //STEP 6: Regenerate code if any errors
            if (!validSyntax || !validSemantics) {
                logger.info { "Invalid response. Regenerating code..." }
            } else {
                logger.info { "Valid response. Proceeding..." }
            }

            //STEP 7: Store question, prompt, and response in MongoDB
            storeInMongoDB(jsonRequest, prompt, parsedCode, validSyntax, validSemantics)

            //STEP 8: Return response
            val response = buildResponse(parsedCode, prompt, validSyntax, validSemantics)
            call.respondText(response, ContentType.Application.Json)
        }
        get("/api/v1/history") {
            val limit = call.parameters["limit"]?.toInt() ?: 10
            val response = getHistory(limit)
            call.respondText(response, ContentType.Application.Json)
        }
        get("/api/v1/schema") {
            val collectionName = call.parameters["collection"] ?: "sample"
            val response = getSampleSchema(collectionName)
            call.respondText(response, ContentType.Application.Json)
        }
        get("/api/v1/collection_list") {
            val response = getCollectionList()
            call.respondText(response, ContentType.Application.Json)
        }
        post("/api/v1/create_embedding") {
            //STEP 1: Get text from request
            val request = call.receiveText() //TODO: Can we use call.receive<PredictRequest>() instead?
            val embeddings = storeEmbeddings(request)

            call.respondText(GSON.toJson(embeddings), ContentType.Application.Json)
        }
        post("/api/v1/get_schema_for_question") {
            val question = call.receiveText()
            val schema = getSchemaForQuestionVS(question)
            call.respondText(GSON.toJson(schema), ContentType.Application.Json)
        }
    }
}



