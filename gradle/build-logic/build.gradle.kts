plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)
    implementation(libs.plugins.kotlin.serialization.dep)
    implementation(libs.plugins.mavencentral.dep)
    implementation(libs.plugins.kfx.dep)
    implementation(libs.plugins.foojay.dep)
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
