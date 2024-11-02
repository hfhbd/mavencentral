plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(11)

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(projects.centralApi)
    compileOnly(libs.ktor.client.java)
    compileOnly(libs.ktor.client.logging)
    compileOnly(libs.ktor.client.content.negotiation)
    compileOnly(libs.ktor.serialization.kotlinx.json)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

val storeVersion by tasks.registering(StoreVersion::class) {
    version.put("centralApi", "${group}:central-api:${version}")
    version.put("ktorJava", libs.ktor.client.java.toString())
    version.put("ktorLogging", libs.ktor.client.logging.toString())
    version.put("ktorContent", libs.ktor.client.content.negotiation.toString())
    version.put("ktorJson", libs.ktor.serialization.kotlinx.json.toString())
}

sourceSets.main {
    kotlin.srcDir(storeVersion)
}

gradlePlugin.plugins.configureEach {
    displayName = "hfhbd mavencentral Gradle Plugin"
    description = "hfhbd mavencentral Gradle Plugin"
}

configurations.configureEach {
    if (isCanBeConsumed) {
        attributes {
            attribute(
                GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                objects.named(GradleVersion.version("8.11").version)
            )
        }
    }
}
