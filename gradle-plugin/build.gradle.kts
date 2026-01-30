plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(21)

dependencies {
    implementation(projects.gradleWorker)
}

gradlePlugin.plugins.configureEach {
    displayName = "hfhbd mavencentral Gradle Plugin"
    description = "hfhbd mavencentral Gradle Plugin"
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
