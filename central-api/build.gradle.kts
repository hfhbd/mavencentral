plugins {
    id("kotlinSetup")
    id("java-test-fixtures")
}

dependencies {
    api(libs.serialization.json)
    api(libs.ktor.client.core)
    testFixturesApi(libs.ktor.server.core)
}
