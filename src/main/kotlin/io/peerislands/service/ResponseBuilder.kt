package io.peerislands.service

import io.peerislands.GSON

fun buildResponse(parsedCode: String, prompt: String,
                  validSyntax: Boolean, validSemantics: Boolean): String {
    val response = mapOf(
        "code" to parsedCode,
        "prompt" to prompt,
        "validSyntax" to validSyntax,
        "validSemantics" to validSemantics
    )
    return GSON.toJson(response)
}
