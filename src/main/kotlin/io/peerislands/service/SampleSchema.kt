package io.peerislands.service

import io.peerislands.logger
import io.peerislands.mongoClient
import org.bson.Document

fun getSampleSchema(collectionName: String): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("sample_schema")
    val filter = Document()
        .append("collection", collectionName)

    val schema = collection.find(filter).first()
    logger.info { "Schema: $schema" }

    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(schema)
}

fun getCollectionList(): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("sample_schema")

    val projection = Document()
        .append("collection", 1)
        .append("_id", 0)

    val collectionList = collection
        .find()
        .projection(projection)
        .toList()
    logger.info { "Collection List: $collectionList" }

    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(collectionList)
}