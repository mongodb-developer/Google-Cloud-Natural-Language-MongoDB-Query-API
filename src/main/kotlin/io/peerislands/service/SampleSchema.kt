package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.GSON
import io.peerislands.genAIDatabase
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.SampleSchema")
fun getSampleSchema(collectionName: String): String {
    val collection = genAIDatabase.getCollection("sample_schema")
    val filter = Document()
        .append("collection", collectionName)

    val schema = collection.find(filter).first()
    logger.info ( "Schema: $schema" )

    return GSON.toJson(schema)
}

fun getCollectionList(): String {
    val collection = genAIDatabase.getCollection("schema_embeddings")

    val collectionList = collection
        .distinct("db_collection", String::class.java).toList()
    logger.info ( "Collection List: $collectionList" )

    return collectionList.toString()
}