import io.github.hfhbd.kfx.openapi.OpenApi

plugins {
    id("setup")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.hfhbd.kfx")
}

kotlin.jvmToolchain(21)

dependencies {
    compileOnly(gradleApi())

    compileOnly(libs.ktor.client.java)
    compileOnly(libs.ktor.client.logging)
    compileOnly(libs.ktor.client.content.negotiation)
    compileOnly(libs.ktor.serialization.kotlinx.json)

    testFixturesImplementation(libs.ktor.server.core)
    testFixturesImplementation(libs.ktor.server.content.negotiation)
    testFixturesImplementation(libs.ktor.serialization.kotlinx.json)
    testFixturesImplementation(kotlin("test-junit5"))
}

val storeVersion by tasks.registering(StoreVersion::class) {
    version.put("ktorJava", libs.ktor.client.java.get().toString())
    version.put("ktorLogging", libs.ktor.client.logging.get().toString())
    version.put("ktorClientContentNegotiation", libs.ktor.client.content.negotiation.get().toString())
    version.put("ktorSerializationKotlinxJson", libs.ktor.serialization.kotlinx.json.get().toString())
}

sourceSets.main {
    kotlin.srcDir(storeVersion)
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
        implementation(libs.ktor.client.java)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.server.test.host)
    }
}

publishing.publications.register<MavenPublication>("maven") {
    from(components["java"])
}
