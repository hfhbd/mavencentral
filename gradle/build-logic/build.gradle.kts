plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)

    compileOnly(libs.ktor.client.java)
    compileOnly(libs.ktor.client.logging)
    compileOnly(libs.ktor.client.content.negotiation)
    compileOnly(libs.ktor.serialization.kotlinx.json)
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
