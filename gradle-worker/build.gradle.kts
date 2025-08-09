import io.github.hfhbd.kfx.openapi.OpenApi

plugins {
    id("setup")
    id("io.github.hfhbd.kfx")
}

dependencies {
    api(gradleApi())

    api(libs.ktor.client.core)
    api(libs.ktor.client.logging)
    api(libs.serialization.json)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.client.content.negotiation)

    implementation(libs.ktor.client.java)

    testFixturesImplementation(libs.ktor.server.core)
    testFixturesImplementation(libs.ktor.server.content.negotiation)
    testFixturesImplementation(kotlin("test-junit5"))
}

kfx {
    register("mavenCentralClient", OpenApi::class) {
        files.from(file("central.json"))
        dependencies {
            compiler(kotlin())
            compiler(kotlinxJson())
            compiler(ktorClient())
        }

        packageName.set("io.github.hfhbd.mavencentral.api")

        sourceSets.main {
            usingSourceSet(kotlin)
        }
    }

    register("mavenCentralServer", OpenApi::class) {
        files.from(file("central.json"))
        dependencies {
            compiler(kotlinxJson())
            compiler(ktorServer())
        }

        packageName.set("io.github.hfhbd.mavencentral.api")

        sourceSets.testFixtures {
            usingSourceSet(kotlin)
        }
    }
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(libs.ktor.server.test.host)
    }
}
