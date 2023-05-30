package io.peerislands

import io.peerislands.model.PredictRequest

fun constructPrompt(jsonRequest: PredictRequest): String {
    //          - Infer collection from question
    //          - Infer question type from question
    //          - Update Prompt template with question, schema and relevant examples
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
                            "temperature": 0.3,
                            "maxOutputTokens": 256,
                            "candidateCount": 1,
                        }
                    }
                    """.trimIndent()
}