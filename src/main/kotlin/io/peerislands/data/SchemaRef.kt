package io.peerislands.data

val inspectionKeywords = listOf("inspections", "violations")
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
        type: String
        score: Double
    class_id: Int
""".trimIndent()

val companiesKeywords = listOf("companies", "company")
val companiesSchema = """
    _id: ObjectId
    name: String
    permalink: String
    crunchbase_url: String
    homepage_url: String
    blog_url: String
    blog_feed_url: String
    twitter_username: String
    category_code: String
    number_of_employees: Int
    founded_year: Int
    founded_month: Int
    founded_day: Int
    deadpooled_year: Int
    deadpooled_month: Int
    deadpooled_day: Int
    deadpooled_url: String
    tag_list: String
    alias_list: String
    email_address: String
    phone_number: String
    description: String
    created_at: String
    updated_at: String
    overview: String
    image: String
    products: Array
        name: String
        permalink: String
    relationships: Array
        is_past: Boolean
        title: String
        person: Object
            first_name: String
            last_name: String
            permalink: String
    competitions: Array
        competitor: Object
            name: String
            permalink: String
    providerships: Array
    total_money_raised: Double
    offices: Array
        office: Object
            description: String
            address1: String
            address2: String
            zip_code: String
            city: String
            state_code: String
            country_code: String
            latitude: String
            longitude: String
""".trimIndent()

val moviesKeywords = listOf("movies", "movie")
val moviesSchema = """
    _id: ObjectId
    plot: String
    genres: Array
    runtime: Int
    cast: Array
    poster: String
    title: String
    fullplot: String
    languages: Array
    year: Int
    released: Date
    directors: Array
    rated: String
    awards: Object
        wins: Int
        nominations: Int
        text: String
    lastupdated: Timestamp
    imdb: Object
        rating: Int
        votes: Int
        id: Int
    countries: Arrary
    type: movie,
    tomatoes: Object
        viewer: Object
            rating: Double
            numReviews: Int
            meter: Int
        fresh: Int
        critic: Object
            rating: Double
            numReviews: Int
            meter: Int
        rotten: Int
        lastUpdated: Date
    num_mflix_comments: Int
""".trimIndent()