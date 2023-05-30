package io.peerislands

import io.peerislands.model.PredictRequest

fun constructPayload(jsonRequest: PredictRequest): String {
    //TODO:          - Infer collection from question
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
    val prompt = promptTemplate
        .replace("{{question}}", question)
        .replace("{{schema}}", getSchema("inspections"))
        .replace("{{examples}}", getExamples())
    return prompt
}

private fun getSchema(collection: String): String {
    val schema = """
        _id ObjectId 
        certificate_number int 
        business_name str 
        date str 
        result str 
        sector str 
        address dict
    """.trimIndent()
    return schema
}

private fun getExamples(): String {
    val examples = """
        
    """.trimIndent()
    return examples
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
