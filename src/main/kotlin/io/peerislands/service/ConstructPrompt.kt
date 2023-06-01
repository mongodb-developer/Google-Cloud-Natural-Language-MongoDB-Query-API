package io.peerislands.service

import io.peerislands.data.*
import io.peerislands.logger
import io.peerislands.model.PredictRequest

fun constructPayload(jsonRequest: PredictRequest): String {
    //TODO:          - Get schema from collection
    //TODO:          - Infer question type from question
    //TODO:          - Get relevant examples from the question type
    //TODO:          - Update Prompt template with question, schema and relevant examples
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

private fun constructPrompt(
    question: String,
    userProvidedContext: String, userProvidedExamples: String): String {
    val collection = getCollectionName(question)
    val questionType = evaluateQuestionType(question)
    return promptTemplate
        .replace("{{question}}", question)
        .replace(
            "{{schema}}",
            getSchema(collection).plus("\n").plus(userProvidedContext)
        )
        .replace(
            "{{examples}}",
            getExamples(questionType).plus("\n").plus(userProvidedExamples)
        )
}

fun getCollectionName(question: String): String {
    //TODO:          - Infer collection from question - use keywords
    //Check if question contains any of the following keywords
    return when {
        inspectionKeywords.any { question.contains(it, ignoreCase = true) } -> "inspections"
        gradesKeywords.any { question.contains(it, ignoreCase = true) } -> "grades"
        companiesKeywords.any { question.contains(it, ignoreCase = true) } -> "companies"
        else -> "movies"
    }
}

fun evaluateQuestionType(question: String): String {
    //Check if question contains any of the following keywords
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
    return when (collection) {
        "inspections" -> inspectionSchema
        "grades" -> gradesSchema
        "companies" -> companiesSchema
        else -> ""
    }
}

private fun getExamples(questionType: String): String {
    return when (questionType) {
        "update" -> updateExamples
        "insert" -> insertExamples
        "delete" -> deleteExamples
        "find" -> findExamples
        else -> ""
    }
}

val promptTemplate = """
Generate MongoDB query.
{{question}}
------------------------------------------------------------
Use the schema model below to construct the query.
{{schema}}
------------------------------------------------------------
Use the examples below to construct the query.
{{examples}}
------------------------------------------------------------
""".trimIndent()
