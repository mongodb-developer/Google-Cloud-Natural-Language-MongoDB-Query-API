package io.peerislands.service

import io.peerislands.data.*
import io.peerislands.logger
import io.peerislands.model.PredictRequest

suspend fun constructPayload(jsonRequest: PredictRequest): String {
    val question = jsonRequest.instances[0].prefix
    val userProvidedContext = jsonRequest.instances[0].context
    val userProvidedExamples = jsonRequest.instances[0].examples

    val prompt = constructPrompt(question, userProvidedContext, userProvidedExamples)

    logger.info{ "Prompt: $prompt" }

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
    val schemaContext = getSchemaForQuestionVS(question)
    val collectionName = schemaContext.collectionName.ifEmpty { getCollectionName(question) }
    val collectionSchema = schemaContext.collectionSchema.ifEmpty { getSchema(collectionName) }

    val questionType = evaluateQuestionType(question)
    val examplesFromQuestionType = getExamples(questionType)

    val examples = getExamplesForQuestionVS(question)
    val exampleFromVS = examples.examples.map { it.example }

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
                .plus(exampleFromVS)
                .plus("\n")
                .plus(userProvidedExamples)
        )
}

fun getCollectionName(question: String): String {
    //TODO:          - Infer collection from question - can we use Vector Search?
    return when {
        inspectionKeywords.any { question.contains(it, ignoreCase = true) } -> "inspections"
        gradesKeywords.any { question.contains(it, ignoreCase = true) } -> "grades"
        companiesKeywords.any { question.contains(it, ignoreCase = true) } -> "companies"
        moviesKeywords.any { question.contains(it, ignoreCase = true) } -> "movies"
        else -> "movies"
    }
}

fun evaluateQuestionType(question: String): String {
    //TODO:          - Infer question type - can we use Vector Search?
    val questionType = when {
        updateKeywords.any { question.contains(it, ignoreCase = true) } -> "update"
        insertKeywords.any { question.contains(it, ignoreCase = true) } -> "insert"
        deleteKeywords.any { question.contains(it, ignoreCase = true) } -> "delete"
        findKeywords.any { question.contains(it, ignoreCase = true) } -> "find"
        else -> "find"
    }
    return questionType
}

private fun getSchema(collection: String): String {
    //TODO:          - Get collection schema - can we use Vector Search?
    return when (collection) {
        "inspections" -> inspectionSchema
        "grades" -> gradesSchema
        "companies" -> companiesSchema
        "movies" -> moviesSchema
        else -> ""
    }
}

private fun getExamples(questionType: String): String {
    //TODO:          - Get Relevant examples - can we use Vector Search?
    return when (questionType) {
        "update" -> updateExamples
        "insert" -> insertExamples
        "delete" -> deleteExamples
        "find" -> findExamples
        else -> ""
    }
}

//TODO: Improve the prompt template
val promptTemplate = """
Generate MongoDB query. 
Output should be a valid MongoDB query. 
We should be able to run the query in mongo shell.
Question: {{question}}
------------------------------------------------------------
Use the schema model below to construct the query.
{{schema}}
------------------------------------------------------------
Use the examples below to construct the query.
{{examples}}
------------------------------------------------------------
""".trimIndent()
