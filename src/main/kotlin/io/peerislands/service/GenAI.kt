package io.peerislands.service

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.peerislands.*
import io.peerislands.model.SchemaEmbeddingsRequest
import io.peerislands.model.EmbeddingsResponse
import io.peerislands.model.ExampleEmbeddingsRequest
import org.bson.Document

suspend fun callGenAIPredict(prompt: String): HttpResponse {
    val token = credentials.refreshAccessToken() //TODO: Is this the right way to get the token?

    //REST API Call
    val response: HttpResponse = client.request(GENAI_PREDICT_ENDPOINT) {
        method = HttpMethod.Post
        headers {
            append("Authorization", "Bearer ${token.tokenValue}")
            append("Content-Type", "application/json")
        }
        setBody(prompt)
    }
    return response
}

suspend fun getTextEmbeddings(text: String): EmbeddingsResponse {
//    GENAI_TEXT_EMBEDDING_ENDPOINT
    val token = credentials.refreshAccessToken() //TODO: Is this the right way to get the token?

    val payload = """
            {
                "instances": [
                    {
                    "content": "$text"
                    }
                ],
            }
            """
        .trimIndent()

    //REST API Call
    val response = client.request(GENAI_TEXT_EMBEDDING_ENDPOINT) {
        method = HttpMethod.Post
        headers {
            append("Authorization", "Bearer ${token.tokenValue}")
            append("Content-Type", "application/json; charset=utf-8")
        }
        setBody(payload)
    }.bodyAsText()
    return GSON.fromJson(response, EmbeddingsResponse::class.java)
}

suspend fun storeSchemaEmbeddings(schemaEmbeddingsRequest: SchemaEmbeddingsRequest): EmbeddingsResponse {
    val text = "This is the schema for ${schemaEmbeddingsRequest.collectionName} collection: "
        .plus("\n")
        .plus(schemaEmbeddingsRequest.schema)
        .plus("\n")
        .plus("Keywords: ")
        .plus("\n")
        .plus(schemaEmbeddingsRequest.keywords)

    //STEP 2: Call GEN AI text-embedding endpoint
    val embeddings = getTextEmbeddings(text)
    logger.info { "embeddings: $embeddings" }

    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("schema_embeddings")
    val document = Document()
        .append("collectionName", schemaEmbeddingsRequest.collectionName)
        .append("schema", schemaEmbeddingsRequest.schema)
        .append("keywords", schemaEmbeddingsRequest.keywords)
        .append("embeddings", embeddings.predictions[0].embeddings.values)
    collection.insertOne(document)

    return embeddings
}

suspend fun storeExampleEmbeddings(exampleEmbeddingsRequest: ExampleEmbeddingsRequest): EmbeddingsResponse {
    val exampleText = "This is an example for ${exampleEmbeddingsRequest.operation}: "
        .plus("\n")
        .plus(exampleEmbeddingsRequest.example)
        .plus("\n")
        .plus("Keywords: ")
        .plus("\n")
        .plus(exampleEmbeddingsRequest.keywords)

    //STEP 2: Call GEN AI text-embedding endpoint
    val embeddings = getTextEmbeddings(exampleText)
    logger.info { "embeddings: $embeddings" }

    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("example_embeddings")
    val document = Document()
        .append("operation", exampleEmbeddingsRequest.operation)
        .append("example", exampleEmbeddingsRequest.example)
        .append("keywords", exampleEmbeddingsRequest.keywords)
        .append("embeddings", embeddings.predictions[0].embeddings.values)
    collection.insertOne(document)

    return embeddings
}
