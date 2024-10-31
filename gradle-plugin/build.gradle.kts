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
    compileOnly(projects.runtime)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

val storeVersion by tasks.registering(StoreVersion::class)

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
