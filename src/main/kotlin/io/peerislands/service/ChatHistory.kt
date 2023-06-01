package io.peerislands.service

import io.peerislands.logger
import io.peerislands.model.PredictRequest
import io.peerislands.mongoClient
import org.bson.Document

fun storeInMongoDB(jsonRequest: PredictRequest, prompt: String, parsedCode: String) {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("history")
    val document = Document()
        .append("question", jsonRequest.instances[0].prefix)
        .append("prompt", prompt)
        .append("code", parsedCode)
    val result = collection.insertOne(document)
    logger.info { "Inserted document: $result" }
}

fun getHistory(limit: Int): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("history")
    val documents = collection.find().limit(limit)
    val history = mutableListOf<Document>()
    documents.forEach { doc -> history.add(doc) }
    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(history)
}