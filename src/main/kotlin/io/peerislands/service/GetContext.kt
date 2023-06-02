package io.peerislands.service

import io.peerislands.model.Context
import io.peerislands.mongoClient
import org.bson.Document


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