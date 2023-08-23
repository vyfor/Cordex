plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "me.blast"
version = "0.2"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.javacord:javacord:3.9.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

kotlin {
    jvmToolchain(8)
}