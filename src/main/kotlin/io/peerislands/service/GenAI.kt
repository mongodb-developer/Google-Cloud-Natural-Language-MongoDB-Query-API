package io.peerislands.service

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.peerislands.*
import io.peerislands.model.request.SchemaEmbeddingsRequest
import io.peerislands.model.response.EmbeddingsResponse
import io.peerislands.model.request.ExampleEmbeddingsRequest
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.GenAI")
suspend fun callGenAIPredict(prompt: String): HttpResponse {
    val token = getGCloudAccessToken()

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
    val token = getGCloudAccessToken()

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
    logger.info ( "embeddings: $embeddings" )

    val collection = genAIDatabase.getCollection("schema_embeddings")
    val document = Document(mapOf(
        "collectionName" to schemaEmbeddingsRequest.collectionName,
        "schema" to schemaEmbeddingsRequest.schema,
        "keywords" to schemaEmbeddingsRequest.keywords,
        "embeddings" to embeddings.predictions[0].embeddings.values
    ))
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
    logger.info ( "embeddings: $embeddings" )

    val collection = genAIDatabase.getCollection("example_embeddings")
    val document = Document(mapOf(
        "operation" to exampleEmbeddingsRequest.operation,
        "example" to exampleEmbeddingsRequest.example,
        "keywords" to exampleEmbeddingsRequest.keywords,
        "embeddings" to embeddings.predictions[0].embeddings.values
    ))
    collection.insertOne(document)

    return embeddings
}
