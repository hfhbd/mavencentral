plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.serialization.json)
    api(libs.ktor.client.core)
    testFixturesApi(libs.ktor.server.core)
}
