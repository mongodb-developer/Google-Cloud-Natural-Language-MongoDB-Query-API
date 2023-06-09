package io.peerislands.service

import io.ktor.util.logging.*
import io.peerislands.data.*
import io.peerislands.genAIDatabase
import io.peerislands.model.request.PredictRequest
import org.bson.Document

private val logger = KtorSimpleLogger("io.peerislands.service.ConstructPrompt")
private const val USE_SCHEMA = false
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

private fun constructPrompt(
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
    val schemaField = if (USE_SCHEMA) "schema" else "example"
    val schemaCollection = genAIDatabase.getCollection("schema_embeddings")
    val projection = Document("collectionName", 1)
        .append(schemaField, 1)
        .append("keywords", 1)
        .append("_id", 0)
    val results = schemaCollection.find().projection(projection).toList()

    results.forEach {
        val collectionName = it["collectionName"] as String
        val keywords = it["keywords"] as List<String>
        val schema = it[schemaField] as String
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
    val examplesCollection = genAIDatabase.getCollection("example_embeddings")
    val projection = Document("operation", 1)
        .append("example", 1)
        .append("keywords", 1)
        .append("_id", 0)
    val filter = Document("keywords", questionType)
        .append("sequence", Document("\$gt", 0L))
    val sort = Document("sequence", 1)
    val results = examplesCollection.find(filter).projection(projection).sort(sort).toList()

    var examples = ""
    results.forEach {
        val index = results.indexOf(it)
        val exampleNum = 1 + index
        val operation = it["operation"] as String
        val example = it["example"] as String
        examples = examples.plus("## Example ${exampleNum} of ${operation}: ##\n")
            .plus(example)
            .plus("\n")
    }
    return examples
}

//TODO: Improve the prompt template
val promptTemplate = """
***** General Instructions Start ***** 
Convert the following natural language query into a MongoDB Query.
Follow the instructions below to generate the query.
Rule 1: Generate the query for the latest version of MongoDB.
Rule 2: Infer collection name from question.
Rule 3: Use ${'$'}expr operator when comparing two fields in same document. Use ${'$'} when referring to fields.
Rule 4: Use quotes for field names.
Rule 5: We should be able to run the query in mongo shell.
Rule 6: Generate the simplest MongoDB query possible.
Rule 7: Pay attention to the schema provided. Field names should match the schema.
When including a field from nested object using dot notation, include the parent object. And enclose them in quotes. For example: { 'address.city' : 'New York' }
When referring to fields in an array of nested objects, use the ${'$'}elemMatch operator. For example: { 'address': ${'$'}elemMatch: { 'city': 'New York' } }
Rule 9: Use the provided examples for guidance.
Rule 10: count() is deprecated. Use countDocuments or estimatedDocumentCount. 
***** General Instructions End *****
############################################################
Question: {{question}}
############################################################
Schema model:
{{schema}}
############################################################
Example MongoDB Queries for reference:
{{examples}}
############################################################
""".trimIndent()
