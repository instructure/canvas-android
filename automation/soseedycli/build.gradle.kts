import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", Versions.KOTLIN))
    }
}

plugins {
    application
    jacoco
}

apply {
    plugin("kotlin")
}

application {
    mainClassName = "SoSeedy"
}

repositories {
    jcenter()
}

dependencies {
    // This project requires a newer version of Coroutines than what is defined in GlobalDependencies.kt
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.22.3")
    compile(Libs.PICOCLI)
    compile(project(":dataseedingapi"))
    compile(kotlin("stdlib-jre8", Versions.KOTLIN))

    /* Test Dependencies */
    testImplementation(Libs.JUNIT)
}

// gradle kotlin-dsl has an issue when this is imported from another file via "applyFrom(file)"
// https://github.com/gradle/kotlin-dsl/issues/751
jacoco {
    toolVersion = Versions.JACOCO
}

tasks.withType<JacocoReport> {
    reports {
        html.isEnabled = true
    }
}

task("fatJar", type = Jar::class) {
    baseName = "${project.name}"
    manifest {
        attributes.apply {
            put("Main-Class", "com.instructure.soseedy.SoSeedy")
        }
    }
    from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}
