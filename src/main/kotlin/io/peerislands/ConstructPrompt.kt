package io.peerislands

import io.peerislands.model.PredictRequest

fun constructPrompt(jsonRequest: PredictRequest): String {
    //TODO:          - Infer collection from question
    //TODO:          - Get schema from collection
    //TODO:          - Infer question type from question
    //TODO:          - Get relevant examples from the question type
    //TODO:          - Update Prompt template with question, schema and relevant examples
    return """
            {
                "instances": [
                    {
                    "prefix": "${jsonRequest.instances[0].prefix}",
                    "suffix": "${jsonRequest.instances[0].suffix}"
                    }
                ],
                "parameters": {
                    "task": "GENERATION",
                    "temperature": ${jsonRequest.parameters.temperature},
                    "maxOutputTokens": 512,
                    "candidateCount": 1,
                }
            }
            """.trimIndent()
}