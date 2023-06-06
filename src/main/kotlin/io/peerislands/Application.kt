package io.peerislands

import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.peerislands.plugins.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.util.logging.*

private val logger = KtorSimpleLogger("io.peerislands.Application")
fun main() {
    logger.info("Starting server at http://localhost:8080")
    //TODO: Get port from application.yaml
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        anyHost()
    }
    configureRouting()
}

//gcloud credentials
val credentials: GoogleCredentials = GoogleCredentials.getApplicationDefault()

//REST API Client
val client = HttpClient() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}

//Get endpoint from Environment Variables. Convert to String
val GENAI_PREDICT_ENDPOINT = try {
    System.getenv()["GENAI_PREDICT_ENDPOINT"].toString()
} catch (e: Exception) {
    logger.error("Error getting GENAI_PREDICT_ENDPOINT from Environment Variables: $e")
    throw e
}

val GENAI_TEXT_EMBEDDING_ENDPOINT = try {
    System.getenv()["GENAI_TEXT_EMBEDDING_ENDPOINT"].toString()
} catch (e: Exception) {
    logger.error ( "Error getting GENAI_TEXT_EMBEDDING_ENDPOINT from Environment Variables: $e" )
    throw e
}


//Create an inject Mongo client
val MONGODB_URI = try {
    System.getenv()["MONGODB_URI"].toString()
} catch (e: Exception) {
    logger.error ( "Error getting MONGODB_URI from Environment Variables: $e" )
    throw e
}
val mongoClient = createMongoClient()
val genAIDatabase: MongoDatabase = mongoClient.getDatabase("genai")

fun createMongoClient(): MongoClient {
    return MongoClients.create(MONGODB_URI)
}

val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
