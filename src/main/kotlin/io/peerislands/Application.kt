package io.peerislands

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.peerislands.plugins.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.util.logging.*
import java.lang.System.getenv

private val logger = KtorSimpleLogger("io.peerislands.Application")

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        anyHost()
    }
    install(ContentNegotiation){
        json()
    }
    configureRouting()
}

fun getGCloudAccessToken(): AccessToken {
    return try {
        credentials.refreshIfExpired()
        credentials.accessToken
    } catch (e: Exception) {
        logger.error("Error getting Google Access Token")
        throw e
    }
}

//gcloud credentials
private val credentials: GoogleCredentials = GoogleCredentials.getApplicationDefault()

//REST API Client
val client = HttpClient {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}

val ENV: MutableMap<String, String> = getenv()

//Get endpoint from Environment Variables. Convert to String
val GENAI_PREDICT_ENDPOINT = try {
    ENV["GENAI_PREDICT_ENDPOINT"].toString()
} catch (e: Exception) {
    logger.error("Error getting GENAI_PREDICT_ENDPOINT from Environment Variables")
    throw e
}

val GENAI_TEXT_EMBEDDING_ENDPOINT = try {
    ENV["GENAI_TEXT_EMBEDDING_ENDPOINT"].toString()
} catch (e: Exception) {
    logger.error ( "Error getting GENAI_TEXT_EMBEDDING_ENDPOINT from Environment Variables" )
    throw e
}


//Create an inject Mongo client
val MONGODB_URI = try {
    ENV["MONGODB_URI"].toString()
} catch (e: Exception) {
    logger.error ( "Error getting MONGODB_URI from Environment Variables" )
    throw e
}
val GENAI_DB = try {
    ENV["GENAI_DB"].toString()
} catch (e: Exception) {
    logger.error( "Error getting GENAI_DB from Environment Variables" )
    logger.error("Setting GENAI_DB to default value: genai")
    "genai"
}

val mongoClient = createMongoClient()
val genAIDatabase: MongoDatabase = mongoClient.getDatabase(GENAI_DB)

fun createMongoClient(): MongoClient {
    return MongoClients.create(MONGODB_URI)
}

val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
