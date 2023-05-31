package io.peerislands

import io.peerislands.model.PredictRequest

fun constructPayload(jsonRequest: PredictRequest): String {
    //TODO:          - Get schema from collection
    //TODO:          - Infer question type from question
    //TODO:          - Get relevant examples from the question type
    //TODO:          - Update Prompt template with question, schema and relevant examples
    val question = jsonRequest.instances[0].prefix

    val prompt = constructPrompt(question)

    print("Prompt: $prompt")

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
                    "maxOutputTokens": 512,
                    "candidateCount": 1,
                }
            }
            """
        .trimIndent()
    return payload.toString()
}

private fun constructPrompt(question: String): String {
    val collection = getCollectionName(question)
    val questionType = evaluateQuestionType(question)
    return promptTemplate
        .replace("{{question}}", question)
        .replace("{{schema}}", getSchema(collection))
        .replace("{{examples}}", getExamples(questionType))
}

fun getCollectionName(question: String): String {
    //TODO:          - Infer collection from question - use keywords
    //Check if question contains any of the following keywords
    return when {
        inspectionKeywords.any { question.contains(it, ignoreCase = true) } -> "inspections"
        gradesKeywords.any { question.contains(it, ignoreCase = true) } -> "grades"
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
---
Use the schema model below to construct the query.
{{schema}}
---
Use the examples below to construct the query.
{{examples}}
---
""".trimIndent()
