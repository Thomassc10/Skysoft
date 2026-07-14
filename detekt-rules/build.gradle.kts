import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val detektVersion = providers.gradleProperty("detekt.version").get()
val javaVersion = 25

dependencies {
    compileOnly("dev.detekt:detekt-api:$detektVersion")
}

kotlin {
    jvmToolchain(javaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
}
