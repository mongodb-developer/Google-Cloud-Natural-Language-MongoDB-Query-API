package io.peerislands

val inspectionKeywords = listOf("inspections", "businesses", "violations")
val inspectionSchema = """
    _id: ObjectId
    id: String
    certificate_number: Int
    business_name: String
    date: String
    result: String
    sector: String
    address: Object
        city: String
        zip: String
        street: String
        number: String
""".trimIndent()

val gradesKeywords = listOf("grades", "grade")
val gradesSchema = """
    _id: ObjectId
    student_id: Int
    scores: Array
        score: Object
            type: String
            score: Double
    class_id: Int
""".trimIndent()