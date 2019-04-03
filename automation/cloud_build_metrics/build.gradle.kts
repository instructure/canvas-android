import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "cloud_build_metrics"
version = "1.0-SNAPSHOT"

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
    kotlin("jvm") version Versions.KOTLIN

    // gradle useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.9"
    id("com.github.ben-manes.versions") version "0.21.0"
}

apply {
    plugin("kotlin")
}

application {
    mainClassName = "main"
}

repositories {
    jcenter()
}

dependencies {
    implementation(project("cloud_build_api"))
    implementation(kotlin("stdlib-jdk8", Versions.KOTLIN))

    // mockito inline enables mocking of final classes by default
    testImplementation("org.mockito:mockito-inline:2.25.1")
    testImplementation("junit:junit:4.13-beta-2")
    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
    testImplementation("org.hamcrest:java-hamcrest:2.0.0.0")
}
// Fix Exception in thread "main" java.lang.NoSuchMethodError: com.google.common.util.concurrent.MoreExecutors.directExecutor()Ljava/util/concurrent/Executor;
// Ensure all deps are using the same modern version of guava
configurations.all {
    resolutionStrategy {
        // https://search.maven.org/search?q=a:guava%20g:com.google.guava
        force("com.google.guava:guava:26.0-jre")
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
}

val javaVersion = "1.8"
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Output full test results to console
// Avoids having to read the HTML report
tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// gradle fatJar
// java -jar build/libs/cloud_build_metrics-all-1.0-SNAPSHOT.jar
task("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes.apply {
            put("Main-Class", "tasks.Main")
        }
    }
    from(configurations.runtimeClasspath.get().map { file ->
        if (file.isDirectory) file else zipTree(file)
    })
    with(tasks["jar"] as CopySpec)
}
