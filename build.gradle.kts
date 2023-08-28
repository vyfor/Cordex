plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    `maven-publish`
}

group = "com.github.reblast"
version = "0.2.1"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    api("org.javacord:javacord:3.9.0-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("net.fellbaum:jemoji:1.1.6")
    
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

kotlin {
    jvmToolchain(8)
}

val sourcesJar = task<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks {
    build {
        dependsOn(sourcesJar)
        dependsOn(jar)
        dependsOn(dokkaHtml)
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            
            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(tasks.dokkaHtml)
        }
    }
}