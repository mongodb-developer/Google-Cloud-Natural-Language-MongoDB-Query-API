package io.peerislands

import com.mongodb.client.MongoClients
import org.bson.BsonDocument

fun validateResponse(answer: String): Boolean {
    //          - Check for syntax errors
    val validSyntax = validateSyntax(answer)
    //          - Check for semantic errors - field names, data types, etc.
    val validSemantics = validateSemantics(answer)

    return validSyntax && validSemantics
}

fun validateSemantics(answer: String): Boolean {
    return true
}

fun validateSyntax(answer: String): Boolean {
    //Get find section of the query
    val findSection = answer

    val client = MongoClients.create()
    val db = client.getDatabase("test")
    val collection = db.getCollection("test")
    return try {
        val query = BsonDocument.parse(findSection)
        collection.find(query)
        true
    } catch (e: Exception) {
        false
    }
}

