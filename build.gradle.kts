val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.0"
}

group = "io.peerislands"
version = "0.0.1"
application {
    mainClass.set("io.peerislands.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.0")
    implementation("io.ktor:ktor-server-mustache-jvm:2.3.0")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    implementation("io.ktor:ktor-jackson:1.6.8")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("com.google.cloud:google-cloud-aiplatform:3.19.0")

    implementation("io.ktor:ktor-server-cors:$ktorVersion")

    //GSON
    implementation("com.google.code.gson:gson:2.8.6")
}