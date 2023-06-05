package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.data.*
import io.peerislands.model.request.PredictRequest
import io.peerislands.mongoClient
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.ConstructPrompt")
suspend fun constructPayload(jsonRequest: PredictRequest): String {
    val question = jsonRequest.instances[0].prefix
    val userProvidedContext = jsonRequest.instances[0].context
    val userProvidedExamples = jsonRequest.instances[0].examples

    val prompt = constructPrompt(question, userProvidedContext, userProvidedExamples)

    logger.info( "Prompt: $prompt" )

    val payload = """
            {
                "instances": [
                    {
                    "prefix": "$prompt",
                    "suffix": ""
                    }
                ],
                "parameters": {
                    "task": "GENERATION",
                    "temperature": ${jsonRequest.parameters.temperature},
                    "maxOutputTokens": ${jsonRequest.parameters.maxOutputTokens},
                    "candidateCount": 1,
                }
            }
            """
        .trimIndent()
    return payload.toString()
}

private suspend fun constructPrompt(
    question: String,
    userProvidedContext: String, userProvidedExamples: String): String {
    val collectionSchema = getSchema(question)

    val questionType = evaluateQuestionType(question)
    val examplesFromQuestionType = getExamples(questionType)

    //For schema include userProvidedContext if it is not empty. Otherwise, use the schema from VS
    return promptTemplate
        .replace("{{question}}", question)
        .replace(
            "{{schema}}",
            userProvidedContext.ifEmpty { collectionSchema }
        )
        .replace(
            "{{examples}}",
            examplesFromQuestionType
                .plus("\n")
                .plus(userProvidedExamples)
        )
}

fun getSchema(question: String): String {
    val schemaDB = mongoClient.getDatabase("genai")
    val schemaCollection = schemaDB.getCollection("schema_embeddings")
    val projection = Document("collectionName", 1)
        .append("schema", 1)
        .append("keywords", 1)
        .append("_id", 0)
    val results = schemaCollection.find().projection(projection).toList()

    results.forEach {
        val collectionName = it["collectionName"] as String
        val keywords = it["keywords"] as List<String>
        val schema = it["schema"] as String
        if (keywords.any { question.contains(it, ignoreCase = true) }) {
            logger.info("Found schema for $collectionName")
            return schema
        }
    }
    return ""
}

fun evaluateQuestionType(question: String): String {
    //TODO:          - Infer question type - can we use Vector Search?
    val questionType = when {
        updateKeywords.any { question.contains(it, ignoreCase = true) } -> "update"
        insertKeywords.any { question.contains(it, ignoreCase = true) } -> "insert"
        deleteKeywords.any { question.contains(it, ignoreCase = true) } -> "delete"
        findKeywords.any { question.contains(it, ignoreCase = true) } -> "find"
        aggregateKeywords.any { question.contains(it, ignoreCase = true) } -> "aggregate"
        else -> "find"
    }
    return questionType
}


fun getExamples(questionType: String): String {

    val examplesDB = mongoClient.getDatabase("genai")
    val examplesCollection = examplesDB.getCollection("example_embeddings")
    val projection = Document("operation", 1)
        .append("example", 1)
        .append("keywords", 1)
        .append("_id", 0)
    val filter = Document("keywords", questionType)
    val results = examplesCollection.find(filter).projection(projection).toList()

    val examples = results.map { it["example"] as String }
    return examples.joinToString("\n")
}

//TODO: Improve the prompt template
val promptTemplate = """
Generate the simplest MongoDB query possible. 
Output should be a valid MongoDB query. 
We should be able to run the query in mongo shell.
------------------------------------------------------------
Question: {{question}}
------------------------------------------------------------
Use the schema model below to construct the query.
{{schema}}
------------------------------------------------------------
Use the examples below to construct the query.
{{examples}}
------------------------------------------------------------
""".trimIndent()
