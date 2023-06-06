package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.mongoClient
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.SampleSchema")
fun getSampleSchema(collectionName: String): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("sample_schema")
    val filter = Document()
        .append("collection", collectionName)

    val schema = collection.find(filter).first()
    logger.info ( "Schema: $schema" )

    val gson = com.google.gson.GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(schema)
}

fun getCollectionList(): String {
    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("schema_embeddings")

    val collectionList = collection
        .distinct("db_collection", String::class.java).toList()
    logger.info ( "Collection List: $collectionList" )

    return collectionList.toString()
}