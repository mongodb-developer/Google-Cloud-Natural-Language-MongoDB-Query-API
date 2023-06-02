package io.peerislands.service

import io.peerislands.logger
import io.peerislands.model.PredictRequest
import io.peerislands.mongoClient
import org.bson.Document

fun storeInMongoDB(jsonRequest: PredictRequest,
                   prompt: String,
                   parsedCode: String,
                   validSyntax: Boolean,
                   validSemantics: Boolean) {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("history")
    val document = Document()
        .append("question", jsonRequest.instances[0].prefix)
        .append("context", jsonRequest.instances[0].context)
        .append("examples", jsonRequest.instances[0].examples)
        .append("temperature", jsonRequest.parameters.temperature)
        .append("maxOutputTokens", jsonRequest.parameters.maxOutputTokens)
        .append("prompt", prompt)
        .append("code", parsedCode)
        .append("validSyntax", validSyntax)
        .append("validSemantics", validSemantics)
    val result = collection.insertOne(document)
    logger.info { "Inserted document: $result" }
}

fun getHistory(limit: Int): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("history")

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