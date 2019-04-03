import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "cloud_build_api"
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
    kotlin("jvm")// version Versions.KOTLIN

    // gradle useLatestVersions
    id("se.patrikerdes.use-latest-versions")
    id("com.github.ben-manes.versions")
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
    // retrofit / okhttp
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.0")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation(kotlin("stdlib-jdk8", Versions.KOTLIN))

    // https://search.maven.org/search?q=a:google-cloud-datastore%20g:com.google.cloud
    implementation("com.google.cloud:google-cloud-datastore:1.66.0")

    // https://cloud.google.com/storage/docs/reference/libraries#client-libraries-install-java
    // https://search.maven.org/search?q=a:google-cloud-storage%20g:com.google.cloud
    api("com.google.cloud:google-cloud-storage:1.66.0")

    // Google Sheets https://developers.google.com/sheets/api/quickstart/java
    // https://search.maven.org/search?q=a:google-api-services-sheets%20g:com.google.apis
    api("com.google.apis:google-api-services-sheets:v4-rev20190305-1.28.0")
    // https://search.maven.org/search?q=a:google-api-client%20g:com.google.api-client
    implementation("com.google.api-client:google-api-client:1.28.0")
    // https://search.maven.org/search?q=a:google-oauth-client-jetty%20g:com.google.oauth-client
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.28.0")

    // yaml parsing
    // https://search.maven.org/search?q=a:jackson-databind%20g:com.fasterxml.jackson.core
    api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")

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
