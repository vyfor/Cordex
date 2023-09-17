plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("maven-publish")
}

group = "com.github.reblast"
version = "0.3.2"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
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

val dokka by tasks.register<Jar>("dokka") {
    notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/1217")
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks {
    build {
        dependsOn(sourcesJar)
        dependsOn(jar)
        dependsOn(dokka)
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])
            
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            
            artifact(sourcesJar)
            artifact(dokka)
        }
    }
}