package io.peerislands.service

import com.google.auth.oauth2.GoogleCredentials
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.peerislands.endpoint

suspend fun callGenAI(prompt: String): HttpResponse {
    val credentials: GoogleCredentials = GoogleCredentials.getApplicationDefault()
    val token = credentials.refreshAccessToken() //TODO: Is this the right way to get the token?

    //REST API Call
    val client = HttpClient() {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }
    val response: HttpResponse = client.request(endpoint) {
        method = HttpMethod.Post
        headers {
            append("Authorization", "Bearer ${token.tokenValue}")
            append("Content-Type", "application/json")
        }
        setBody(prompt)
    }
    return response
}