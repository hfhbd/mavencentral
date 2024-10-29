plugins {
    kotlin("jvm")
    id("setup")
}


kotlin.jvmToolchain(8)

dependencies {
    testImplementation(kotlin("test"))
}

java {
    withJavadocJar()
    withSourcesJar()
}
