package io.peerislands.service

import java.util.*

private const val connString = "mongodb://localhost:27017"
fun executeMongoCommand(mql: String, db: String): String {
    println("Executing: $mql")
    val runtime = Runtime.getRuntime()
    val process = runtime.exec(
        arrayOf(
            "mongosh",
            connString.plus("/").plus(db),
            "--eval",
            mql,
            "--quiet"
        )
    )
    process.waitFor()
    val scanner = Scanner(process.inputStream)
    scanner.useDelimiter("\\A").use { s ->
        return if (s.hasNext()) s.next() else ""
    }
}
fun main() {
    val mql = "db.movies.find({\"year\": 1933 }).limit(2)"
    val db = "sample_mflix"
    executeMongoCommand(mql, db)
}