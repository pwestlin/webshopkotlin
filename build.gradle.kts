import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base

    kotlin("jvm") version "1.4.10" apply false
    id("org.springframework.boot") version "2.3.4.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    kotlin("plugin.spring") version "1.4.10" apply false

    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    idea
}

allprojects {
    apply(plugin = "idea")
    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        disabledRules.set(listOf("final-newline,import-ordering"))
    }

    group = "nu.westlin.webshop"

    version = "0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        val implementation by configurations
        implementation(kotlin("stdlib-jdk8"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xallow-result-return-type")
            jvmTarget = "1.8"
        }
    }

    if (project.name in listOf("core", "customer")) {

        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")

        dependencies {
            val implementation by configurations
            implementation(kotlin("stdlib-jdk8"))

            implementation("org.springframework.boot:spring-boot-starter-webflux")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

            val testImplementation by configurations
            testImplementation("org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
            }
            testImplementation(project(":testutils"))
            testImplementation(group = "io.mockk", name = "mockk", version = "1.10.2")
            testImplementation(group = "com.ninja-squad", name = "springmockk", version = "2.0.3")
            testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2")
        }
    }
}
