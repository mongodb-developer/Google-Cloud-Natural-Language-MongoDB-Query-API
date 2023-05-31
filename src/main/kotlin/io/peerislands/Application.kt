package io.peerislands

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.peerislands.plugins.*
import io.ktor.server.plugins.cors.routing.CORS

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        allowHost("localhost:3000")
    }
    configureRouting()
}

//Get endpoint from Environment Variables. Convert to String
val endpoint = System.getenv()["GENAI_ENDPOINT"]!!.toString()