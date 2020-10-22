plugins {
    base
    kotlin("jvm") version "1.4.10" apply false
}

allprojects {

    group = "nu.westlin.webshop"

    version = "0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

/*
dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        // TODO petves: Fix
        archives(it)
    }
}*/
