import io.github.hfhbd.kfx.openapi.OpenApi

plugins {
    id("kotlinSetup")
    id("io.github.hfhbd.kfx")
}

dependencies {
    api(libs.serialization.json)
    api(libs.ktor.client.core)

    testFixturesApi(libs.ktor.server.core)
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
