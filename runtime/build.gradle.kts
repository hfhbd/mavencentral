plugins {
    id("kotlinSetup")
}

kotlin.jvmToolchain(11)

dependencies {
    api(projects.centralApi)
    api(libs.ktor.client.java)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
}
