plugins {
    kotlin("jvm")
    id("setup")
    kotlin("plugin.serialization")
}

kotlin.jvmToolchain(8)

testing.suites.named<JvmTestSuite>("test") {
    useKotlinTest()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing.publications.register<MavenPublication>("gpr") {
    from(components["java"])
}
