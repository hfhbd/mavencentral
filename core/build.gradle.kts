import io.github.hfhbd.kfx.openapi.OpenApi

plugins {
    id("setup")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.hfhbd.kfx")
}

kotlin.jvmToolchain(21)

dependencies {
    implementation(libs.ktor.client.java)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)

    testFixturesApi(libs.ktor.server.core)
    testFixturesApi(libs.ktor.server.content.negotiation)
    testFixturesApi(kotlin("test-junit5"))
}

kfx {
    register("mavenCentralClient", OpenApi::class) {
        files.from(file("central.json"))
        dependencies {
            compiler(kotlinClasses())
            compiler(kotlinxJson())
            compiler(ktorClient())
        }

        packageName.set("io.github.hfhbd.mavencentral.api")

        usingKotlinSourceSet(kotlin.sourceSets.main)
    }

    register("mavenCentralServer", OpenApi::class) {
        files.from(file("central.json"))
        dependencies {
            compiler(kotlinxJson())
            compiler(ktorServer())
        }

        packageName.set("io.github.hfhbd.mavencentral.api")

        usingKotlinSourceSet(kotlin.sourceSets.testFixtures)
    }
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(libs.ktor.server.test.host)
    }
}

publishing.publications.register<MavenPublication>("maven") {
    from(components["java"])
}
