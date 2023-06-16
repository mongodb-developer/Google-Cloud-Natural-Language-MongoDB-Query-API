package io.peerislands.plugins

import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import io.peerislands.GSON
import io.peerislands.model.request.ExampleEmbeddingsRequest
import io.peerislands.model.request.PredictRequest
import io.peerislands.model.request.RunMongoCommandRequest
import io.peerislands.model.request.SchemaEmbeddingsRequest
import io.peerislands.model.response.PredictResponse
import io.peerislands.service.*


private val logger = KtorSimpleLogger("io.peerislands.plugins.Routing")
fun Application.configureRouting() {
    routing {
        post("/api/v1/predict") {
            //STEP 1: Get question from request
            val jsonRequest: PredictRequest = predictRequest()

            //STEP 2: Construct Prompt
            val prompt = constructPayload(jsonRequest)

            //STEP 3: Call GEN AI code-bison endpoint
            val genAIResponse: HttpResponse = callGenAIPredict(prompt)
            val predictResponse = GSON.fromJson(genAIResponse.bodyAsText(), PredictResponse::class.java)

            //STEP 4: Parse response and get answer / code
            val parsedCode: String = parseResponse(predictResponse)
            logger.info ( "parsedCode: $parsedCode" )

            //STEP 5: Run validations
            val (validSyntax, validSemantics) = validateResponse(parsedCode, jsonRequest.instances[0].context)

            //STEP 6: Regenerate code if any errors. //TODO: To be implemented
            if (!validSyntax || !validSemantics) {
                logger.info ( "Invalid response. Regenerating code..." )
            } else {
                logger.info ( "Valid response. Proceeding..." )
            }

            //STEP 7: Store question, prompt, and response in MongoDB. History
            storeInMongoDB(jsonRequest, prompt, predictResponse, parsedCode, validSyntax, validSemantics)

            //STEP 8: Return response
            val response = buildResponse(parsedCode, prompt, validSyntax, validSemantics)
            call.respond(response)
        }

        get("/api/v1/history") {
            val limit = call.parameters["limit"]?.toInt() ?: 10
            val response = getHistory(limit)
            call.respond(response)
        }

        get("/api/v1/schema") {
            val collectionName = call.parameters["collection"] ?: "sample"
            val response = getSampleSchema(collectionName)
            call.respond(response)
        }

        get("/api/v1/collection_list") {
            val response = getCollectionList()
            call.respond(response)
        }

        post("/api/v1/create_schema_embedding") {
            //STEP 1: Get text from request
            val schemaEmbeddingsRequest = call.receive<SchemaEmbeddingsRequest>()
            val embeddings = storeSchemaEmbeddings(schemaEmbeddingsRequest)

            call.respond(embeddings)
        }

        post("/api/v1/create_example_embedding") {
            //STEP 1: Get text from request
            val exampleEmbeddingsRequest = call.receive<ExampleEmbeddingsRequest>()
            val embeddings = storeExampleEmbeddings(exampleEmbeddingsRequest)

            call.respond(embeddings)
        }

        post("/api/v1/get_schema_for_question") {
            val question = call.receiveText()
            val schema = getSchemaForQuestionVS(question)
            call.respond(schema)
        }

        post("/api/v1/get_examples_for_question") {
            val question = call.receiveText()
            val questionType = evaluateQuestionType(question)
            val examplesFromQuestionType = getExamples(questionType)
            call.respond(examplesFromQuestionType)
        }

        post("/api/v1/run_mql") {
            val mongoCommandRequest = call.receive<RunMongoCommandRequest>()
            val response = executeMongoCommand(mongoCommandRequest.command, mongoCommandRequest.db)
            call.respond(response)
        }
    }
}



