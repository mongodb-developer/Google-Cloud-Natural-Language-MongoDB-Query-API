package io.peerislands

import com.google.gson.GsonBuilder

fun buildResponse(parsedCode: String, prompt: String): String {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    val response = mapOf("code" to parsedCode, "prompt" to prompt)
    return gson.toJson(response)
}
