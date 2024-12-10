import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "de.zordid"
version = "1.0-SNAPSHOT"

val kotest = "5.9.1"
val junit = "5.11.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    //implementation("org.jetbrains.kotlinx:multik-core:0.2.0")
    //implementation("org.jetbrains.kotlinx:multik-default:0.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.github.ajalt.mordant:mordant:3.0.1")
    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    implementation("org.slf4j:slf4j-nop:2.0.16")
    //implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.arrow-kt:arrow-core:2.0.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:2.0.0")

    //implementation("org.choco-solver:choco:4.10.14")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest")
    testImplementation("io.kotest:kotest-property:$kotest")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs = listOf(
            "-Xcontext-receivers",
            "-Xconsistent-data-class-copy-visibility",
            "-Xwhen-guards",
            "-Xmulti-dollar-interpolation",
        )
    }
}

java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass.set("AdventOfCodeKt")
}
