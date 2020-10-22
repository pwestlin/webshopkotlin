plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation(group = "io.mockk", name = "mockk", version = "1.10.2")
}