import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.61"
}

group = "cluster_create"
version = "1.0-SNAPSHOT"

repositories {
    maven(url = "http://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/kotlin/kotlinx")
    jcenter()
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // https://developers.google.com/api-client-library/java/apis/container/v1
    // http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.google.apis%22%20AND%20a%3A%22google-api-services-container%22
    implementation("com.google.apis:google-api-services-container:v1-rev54-1.25.0")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:0.40")

    // https://bintray.com/kotlin/ktor/ktor
    val ktorVersion = "0.9.3"
    testImplementation("io.ktor:ktor-server-core:$ktorVersion")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-gson:$ktorVersion")
    testImplementation("ch.qos.logback:logback-classic:1.2.1")
    testImplementation("com.google.code.gson:gson:2.8.2")

    // https://github.com/Kotlin/kotlinx.coroutines/releases
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.23.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes.apply {
            put("Main-Class", "Main")
        }
    }

    // https://stackoverflow.com/questions/47910578/not-able-to-copy-configurations-dependencies-after-upgrading-gradle-plugin-for-a?rq=1
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}
