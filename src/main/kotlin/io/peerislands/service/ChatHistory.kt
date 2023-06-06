package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.genAIDatabase
import io.peerislands.model.request.PredictRequest
import io.peerislands.model.response.PredictResponse
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.ChatHistory")
fun storeInMongoDB(jsonRequest: PredictRequest,
                   prompt: String,
                   predictResponse: PredictResponse,
                   parsedCode: String,
                   validSyntax: Boolean,
                   validSemantics: Boolean) {
    val collection = genAIDatabase.getCollection("history")
    val document = Document()
        .append("question", jsonRequest.instances[0].prefix)
        .append("context", jsonRequest.instances[0].context)
        .append("examples", jsonRequest.instances[0].examples)
        .append("temperature", jsonRequest.parameters.temperature)
        .append("maxOutputTokens", jsonRequest.parameters.maxOutputTokens)
        .append("prompt", prompt)
        .append("response", predictResponse.toString())
        .append("code", parsedCode)
        .append("validSyntax", validSyntax)
        .append("validSemantics", validSemantics)
    val result = collection.insertOne(document)
    logger.info ( "Inserted document: $result" )
}

fun getHistory(limit: Int): String {
    val collection = genAIDatabase.getCollection("history")

    val documents = collection.find()
        .sort(Document("_id", -1))
        .limit(limit)

    val history = mutableListOf<Document>()
    documents.forEach { doc -> history.add(doc) }
    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(history)
}