package io.peerislands

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.peerislands.plugins.*
import io.ktor.server.plugins.cors.routing.CORS

fun main() {
    logger.info { "Starting server at http://localhost:8080" }
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

val logger = mu.KotlinLogging.logger {}

//Get endpoint from Environment Variables. Convert to String
val endpoint = System.getenv()["GENAI_ENDPOINT"]!!.toString()

//Create an inject Mongo client
val mongoClient = createMongoClient()

fun createMongoClient(): MongoClient {
    return MongoClients.create()
}
