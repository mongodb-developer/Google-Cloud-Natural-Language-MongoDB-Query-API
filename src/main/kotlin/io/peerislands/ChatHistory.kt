package io.peerislands

import com.mongodb.client.MongoClients
import io.peerislands.model.PredictRequest
import org.bson.Document

fun storeInMongoDB(jsonRequest: PredictRequest, prompt: String, parsedCode: String) {
    val client = MongoClients.create()
    val db = client.getDatabase("genai")
    val collection = db.getCollection("history")
    val document = Document()
        .append("question", jsonRequest.instances[0].prefix)
        .append("prompt", prompt)
        .append("code", parsedCode)
    val result = collection.insertOne(document)
    client.close()
    println("Inserted document: $result")
}

fun getHistory(limit: Int): String {
    val client = MongoClients.create()
    val db = client.getDatabase("genai")
    val collection = db.getCollection("history")
    val documents = collection.find().limit(limit)
    val history = mutableListOf<Document>()
    documents.forEach { doc -> history.add(doc) }
    client.close()
    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(history)
}