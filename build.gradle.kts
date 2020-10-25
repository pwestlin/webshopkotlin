import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base

    kotlin("jvm") version "1.4.10" apply false
    id("org.springframework.boot") version "2.3.4.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    kotlin("plugin.spring") version "1.4.10" apply false

    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    idea

    id("com.bmuschko.docker-spring-boot-application") version "6.6.1"
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

    if (project.name == "discovery-server") {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")

        apply(plugin = "com.bmuschko.docker-spring-boot-application")
        docker {
            springBootApplication {
                baseImage.set("openjdk:8-alpine")
                images.set(setOf("nu.westlin.${project.rootProject.name}/${project.name}:$version"))
            }
        }

        dependencies {
            val implementation by configurations

            implementation(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR8"))

            implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

            implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

            val testImplementation by configurations
            testImplementation("org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
            }
        }
    }

    if (project.name in listOf("core-service", "customer-service", "product-service", "order-service")) {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")

        apply(plugin = "com.bmuschko.docker-spring-boot-application")
        docker {
            springBootApplication {
                baseImage.set("openjdk:8-alpine")
                images.set(setOf("nu.westlin.${project.rootProject.name}/${project.name}:$version"))
            }
        }

        dependencies {
            val implementation by configurations
            implementation(project(":testdata"))
            implementation(project(":domain"))
            implementation(kotlin("stdlib-jdk8"))

            implementation("org.springframework.boot:spring-boot-starter-webflux")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

            val testImplementation by configurations
            testImplementation("org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
            }
            testImplementation(project(":testutils"))
            testImplementation(group = "io.mockk", name = "mockk", version = "1.10.2")
            testImplementation(group = "com.ninja-squad", name = "springmockk", version = "2.0.3")

            implementation(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR8"))
        }
    }

    if (project.name in listOf("core-service")) {
        dependencies {
            val testImplementation by configurations
            testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2")
        }
    }
}
