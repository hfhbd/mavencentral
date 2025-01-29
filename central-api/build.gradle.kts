plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.serialization.json)
    api(libs.ktor.client.core)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.client.content.negotiation)

    testFixturesApi(libs.ktor.server.core)
    testFixturesApi(libs.ktor.server.content.negotiation)
}
