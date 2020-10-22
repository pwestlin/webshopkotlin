plugins {
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm")
    kotlin("plugin.spring") version "1.4.10"
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

// TODO petves: Where to put common stuff (deps, test-conf etc) for Microservices?

dependencies {
    implementation(project(":domain"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    val kotlinCorotuinesVersion = "1.3.9"
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCorotuinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCorotuinesVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(group = "io.mockk", name = "mockk", version = "1.10.2")
    testImplementation(group = "com.ninja-squad", name = "springmockk", version = "2.0.3")
    //testImplementation "com.github.tomakehurst:wiremock-jre8:2.27.2"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
