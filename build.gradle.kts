plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.speechify"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()

    reports {
        junitXml.required.set(true)
        junitXml.outputLocation.set(file("${buildDir}/test-results/junitXml"))
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/test-results/junitHtml"))
    }
}

kotlin {
    jvmToolchain(17)
}
