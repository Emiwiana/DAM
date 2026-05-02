plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("kapt") version "1.9.22"// Preferred in Kotlin DSL ( Gradle KTS )
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    // AutoService to register the processor automatically
    implementation("com.google.auto.service:auto-service:1.1.1")
    // Required for registering the processor
    kapt ("com.google.auto.service:auto-service:1.1.1")
    // KotlinPoet for generating Kotlin code
    implementation("com.squareup:kotlinpoet:1.14.2")
    // Include the annotations module
    implementation(project(":annotations"))
}

kapt {
    correctErrorTypes = true
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}