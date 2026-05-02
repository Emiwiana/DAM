plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("kapt") // Needed for annotation processing
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin ("stdlib"))

    // Include the annotations module
    implementation(project(":annotations"))
    // Use the annotation processor
    kapt (project(":processor"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}