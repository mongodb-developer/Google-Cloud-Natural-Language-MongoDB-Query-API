@file:Suppress("UNCHECKED_CAST")
package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.model.Context
import io.peerislands.model.Example
import io.peerislands.model.Examples
import io.peerislands.mongoClient
import org.bson.Document


private val logger = KtorSimpleLogger("io.peerislands.service.GetContext")
suspend fun getSchemaForQuestionVS(question: String): Context {
    //Get the embeddings for the question
    val questionEmbeddings = getTextEmbeddings(question)

    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("schema_embeddings")

    val searchStage =
        Document("\$search",
            Document("index", "default")
                .append("knnBeta",
                    Document("vector", questionEmbeddings.predictions[0].embeddings.values.toList())
                        .append("path", "embeddings")
                        .append("k", 1L)
            )
    )

    val pipeline = listOf(searchStage)
    val results = collection.aggregate(pipeline).first()

    val collectionName = results?.get("collectionName") as String
    val collectionSchema = results["schema"] as String
    val collectionKeywords = results["keywords"] as List<String>

    val context = Context(collectionName, collectionSchema, collectionKeywords)

    return context
}


suspend fun getExamplesForQuestionVS(question: String): Examples {
    //Get the embeddings for the question
    val questionEmbeddings = getTextEmbeddings(question)

    val db = mongoClient.getDatabase("genai")
    val collection = db.getCollection("example_embeddings")

    val searchStage =
        Document("\$search",
            Document("index", "default")
                .append("knnBeta",
                    Document("vector", questionEmbeddings.predictions[0].embeddings.values.toList())
                        .append("path", "embeddings")
                        .append("k", 5L)
                )
        )

    val pipeline = listOf(searchStage)
    logger.info ( "pipeline: $pipeline" )
    val results = collection.aggregate(pipeline).toList()

    val examples = results.map {
        val operation = it["operation"] as String
        val example = it["example"] as String
        val exampleKeywords = it["keywords"] as List<String>
        Example(operation, example, exampleKeywords)
    }

    return Examples(examples)
}