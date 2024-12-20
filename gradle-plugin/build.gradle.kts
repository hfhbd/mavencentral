plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(17)

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
    version.put("centralApi", "${project.group}:central-api:${project.version}")
    version.put("ktorJava", libs.ktor.client.java.get().toString())
    version.put("ktorLogging", libs.ktor.client.logging.get().toString())
    version.put("ktorContent", libs.ktor.client.content.negotiation.get().toString())
    version.put("ktorJson", libs.ktor.serialization.kotlinx.json.get().toString())
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
                objects.named(GradleVersion.current().version)
            )
        }
    }
}
