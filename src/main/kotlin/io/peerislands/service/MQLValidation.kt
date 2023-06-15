@file:Suppress("UNCHECKED_CAST")
package io.peerislands.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import io.ktor.util.logging.*
import io.peerislands.data.*
import io.peerislands.mongoClient
import org.bson.Document
import java.util.Scanner

//TODO: Re-design using ANTLR parser or similar

private val logger = KtorSimpleLogger("io.peerislands.service.ChatHistory")
private const val USE_SCHEMA = false
data class ValidationResponse(
    val validSyntax: Boolean,
    val validSemantics: Boolean
)

const val OPEN_PAREN = "("
const val CLOSE_PAREN = ")"
val OBJECT_MAPPER: ObjectMapper = ObjectMapper().registerModule(
    KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, false)
        .configure(KotlinFeature.NullToEmptyMap, false)
        .configure(KotlinFeature.NullIsSameAsDefault, false)
        .configure(KotlinFeature.StrictNullChecks, false)
        .build())
    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

fun validateResponse(answer: String, userContext: String): ValidationResponse {
    val validSyntax: Boolean =
        try {
            validateSyntax(answer)
        } catch (e: Exception) {
            logger.error ( "Error validating response: ${e.message}" )
            false
        }

    val validSemantics: Boolean =
        try {
            validateSemantics(answer, userContext)
        } catch (e: Exception) {
            logger.error ( "Error validating response: ${e.message}" )
            false
        }

    return ValidationResponse(validSyntax, validSemantics)
}

fun validateSemantics(answer: String, userContext: String): Boolean {
    val extractedQuery = extractQuery(answer)
    return when (getOperation(answer)) {
        "insertOne", "insertMany" -> true // No validation for now
        "find" -> validateFindQueryFields(extractedQuery[0] as Map<String, Any>, answer, userContext)
        "aggregate" -> validateAggregateQueryFields(extractedQuery[0] as List<Map<String, Any>>, answer, userContext)
        else -> true // Need to be false. Temporarily returning true to bypass other operations
    }
}

fun validateSyntax(answer: String): Boolean {
    val db = mongoClient.getDatabase("test")
    val collection = db.getCollection("test")
    return try {
        val extractedQuery = extractQuery(answer)
        when (getOperation(answer)) {
            "insertOne" -> collection.insertOne(Document.parse(OBJECT_MAPPER.writeValueAsString(extractedQuery[0] as Map<String, Any>)))
            "insertMany" -> collection.insertMany((extractedQuery[0] as List<Map<String, Any>>).map { item ->
                Document.parse(
                    OBJECT_MAPPER.writeValueAsString(item)
                )
            })
            "aggregate" -> collection.aggregate((extractedQuery[0] as List<Map<String, Any>>).map { item ->
                Document.parse(
                    OBJECT_MAPPER.writeValueAsString(item)
                )
            }).first()
            "find" -> collection.find(Document.parse(OBJECT_MAPPER.writeValueAsString(extractedQuery[0] as Map<String, Any>))).first()
        }
        true
    } catch (e: Exception) {
        false
    }
}

fun extractQuery(answer: String): List<Any> {
    val parsedAnswer = answer.convertToBsonSyntax()
    val funArgs = parsedAnswer.substring(parsedAnswer.indexOf(OPEN_PAREN) + 1, parsedAnswer.indexOf(CLOSE_PAREN))
    return OBJECT_MAPPER.readValue("[$funArgs]", object : TypeReference<List<Any>>() {})
}

fun getOperation(answer: String): String {
    return when {
        answer.contains("insertOne") -> "insertOne"
        answer.contains("insertMany") -> "insertMany"
        answer.contains("aggregate") -> "aggregate"
        // Temporarily handling all other operations as find. Need to add exhaustive list in future
        else -> "find"
    }
}

fun getFieldsFromSchema(vararg schemas: String): List<String> {
    val fieldList = mutableListOf<String>()
    schemas.forEach {
            schema -> fieldList.addAll(getFieldsFromSchema(schema))
    }
    return fieldList
}
fun getFieldsFromSchema(schema: String): List<String> {
    val fieldList = mutableListOf<String>()
    Scanner(schema).use {
            sc ->
        val parentList = mutableListOf<String>()
        var intendCount = 0
        while(sc.hasNextLine()){
            val content = sc.nextLine().split(":")

            while(intendCount != content[0].countPrefixIntend()) {
                parentList.removeLast()
                intendCount--
            }
            if(parentList.isNotEmpty()){
                fieldList.add(parentList.reduce { acc, s -> "$acc.$s" }+"."+content[0].trim())
            }else{
                fieldList.add(content[0].trim())
            }

            if((content[1].trim().lowercase().contains("object") ) or (content[1].trim().lowercase().contains("array") )){
                intendCount++
                parentList.add(content[0].trim())
            }
        }
    }
    return fieldList
}

fun String.countPrefixIntend(intendSpaceCount: Int = 4): Int {
    var spaceCount = 0
    for(char in this) {
        if(char == ' '){
            spaceCount++
        }else {
            break
        }
    }
    return spaceCount/intendSpaceCount
}

fun extractFieldsFromQuery(mQuery: Map<String, Any>): List<String> {
    val resultList = mutableSetOf<String>()
    val excludeOperationList = listOf("\$elemMatch", "\$group", "\$project", "\$sort", "\$dateToString")
    mQuery.entries.forEach {
            entry ->
        if(entry.value is Map<*, *>) {
            if(!excludeOperationList.contains(entry.key)) // Excluding validation on elemMatch and aggregation operations except match
                resultList.addAll(extractFieldsFromQuery(entry.value as Map<String, Any>))
        } else if(entry.value is List<*>) {
            (entry.value as List<*>).forEach {
                    item ->
                if(item is Map<*, *>) {
                    resultList.addAll(extractFieldsFromQuery(item as Map<String, Any>))
                }
            }
        }
        if(!entry.key.startsWith("\$")){
            resultList.add(entry.key.toMongoDBFieldName())
        }
    }
    return resultList.toList()
}

fun String.toMongoDBFieldName(): String {
    // Regex to remove array filter $[*]
    val arrayFilterRegex = Regex(pattern = "\\$\\[.*\\]", options = setOf(RegexOption.IGNORE_CASE))
    // Regex to remove array access by index
    val arrayIndexRegex = Regex(pattern = "\\.[0-9]+", options = setOf(RegexOption.IGNORE_CASE))
    return this.replace(arrayFilterRegex, "").replace(arrayIndexRegex, "")
        .replace("\$","").replace("..",".")
}

fun validateFindQueryFields(query: Map<String, Any>, answer: String, userContext: String): Boolean {
    val schema = userContext.ifEmpty { getSchemaFromAnswer(answer) }
    val fieldList = if (USE_SCHEMA) getFieldsFromSchema(schema) else extractFieldsFromExample(schema)
    return extractFieldsFromQuery(query).all { fieldList.contains(it)  }
}

fun String.convertObjectIdSyntax(): String {
    val regex = Regex(pattern = """ObjectId\("(\w+)"\)""", options = setOf(RegexOption.IGNORE_CASE))
    var resultQuery = this
    regex.findAll(this).forEach {
        val objectId = it.value
        println(objectId)
        resultQuery = resultQuery.replace(objectId, """{"${'$'}oid": "${objectId.substring(objectId.indexOf("\"")+1, objectId.lastIndexOf("\""))}"}""")
    }
    return resultQuery
}

fun validateAggregateQueryFields(query: List<Map<String, Any>>, answer: String, userContext: String): Boolean {
    val schema = userContext.ifEmpty { getSchemaFromAnswer(answer) }
    val fieldList = if (USE_SCHEMA) getFieldsFromSchema(schema) else extractFieldsFromExample(schema)
    val queryFieldList = mutableListOf<String>()
    query.forEach{
        queryFieldList.addAll(extractFieldsFromQuery(it))
    }
    return queryFieldList.all { fieldList.contains(it) }
}

fun String.convertDateSyntax(): String {
    val regex = Regex(pattern = """new\s+Date\([^)]+\)""", options = setOf(RegexOption.IGNORE_CASE))
    var resultQuery = this
    regex.findAll(this).forEach {
        val date = it.value
        resultQuery = resultQuery.replace(
            date,
            """"${date.substring(date.indexOf("\"") + 1, date.lastIndexOf("\""))}""""
        )
    }
    return resultQuery
}

fun String.convertToBsonSyntax(): String {
    return this.convertObjectIdSyntax().convertDateSyntax().replace("new Date()", "\"\"")
}

fun getSchemaFromAnswer(answer: String): String {
    // Assuming collection does not have any periods(.)
    val field = if (USE_SCHEMA) "schema" else "example"
    val collection = answer.split(".")[1]
    val schemaDocument = mongoClient.getDatabase("genai")
        .getCollection("schema_embeddings").find(Filters.eq("collectionName", collection))
        .projection(Projections.include(field)).first()
    return schemaDocument.getString(field)
}

fun extractFieldsFromExample(sampleDocument: String): List<String> {
    val doc = OBJECT_MAPPER.readValue(sampleDocument, object: TypeReference<Map<String, Any>>(){})
    return extractFieldsFromSampleDocument(doc)
}

fun extractFieldsFromSampleDocument(document: Map<String, Any>, parent: String = ""): List<String> {
    val resultList = mutableSetOf<String>()
    document.entries.forEach {
            entry ->
        val fieldKey = if(parent.isNullOrEmpty()) entry.key else "$parent.${entry.key}"
        resultList.add(fieldKey)
        if(entry.value is Map<*, *>) {
            resultList.addAll(extractFieldsFromSampleDocument(entry.value as Map<String, Any>, fieldKey))
        } else if(entry.value is List<*>) {
            (entry.value as List<*>).forEach {
                    item ->
                if(item is Map<*, *>) {
                    resultList.addAll(extractFieldsFromSampleDocument(item as Map<String, Any>, fieldKey))
                }
            }
        }
    }
    return resultList.toList()
}