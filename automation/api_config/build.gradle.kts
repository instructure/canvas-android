import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.51"
}

group = "api_config"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// TODO: 'implementation' deps don't appear in the fat jar
dependencies {
    compile(kotlin("stdlib-jdk8"))

    // https://mvnrepository.com/artifact/com.squareup.wire/wire-schema
    compile("com.squareup.wire:wire-schema:2.3.0-RC1")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:0.40")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes.apply {
            put("Main-Class", "api.config.ParseProto")
        }
    }
    from(configurations.runtime.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}
