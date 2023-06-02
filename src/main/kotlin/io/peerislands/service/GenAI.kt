package io.peerislands.service

import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.peerislands.*
import io.peerislands.model.EmbeddingsRequest
import io.peerislands.model.EmbeddingsResponse
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

suspend fun storeEmbeddings(request: String): EmbeddingsResponse {
    val embeddingsRequest = Gson().fromJson(request, EmbeddingsRequest::class.java)

    val text = "Schema for collection: "
        .plus(embeddingsRequest.collectionName)
        .plus("\n")
        .plus(embeddingsRequest.schema)

    //STEP 2: Call GEN AI text-embedding endpoint
    val embeddings = getTextEmbeddings(text)
    logger.info { "embeddings: $embeddings" }

    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("schema_embeddings")
    val document = Document()
        .append("collectionName", embeddingsRequest.collectionName)
        .append("schema", embeddingsRequest.schema)
        .append("keywords", embeddingsRequest.keywords)
        .append("embeddings", embeddings.predictions[0].embeddings.values)
    collection.insertOne(document)

    return embeddings
}