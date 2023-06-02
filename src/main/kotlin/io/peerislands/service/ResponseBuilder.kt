package io.peerislands.service

import com.google.gson.GsonBuilder

fun buildResponse(parsedCode: String, prompt: String,
                  validSyntax: Boolean, validSemantics: Boolean): String {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    val response = mapOf(
        "code" to parsedCode,
        "prompt" to prompt,
        "validSyntax" to validSyntax,
        "validSemantics" to validSemantics
    )
    return gson.toJson(response)
}
