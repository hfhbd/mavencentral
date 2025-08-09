plugins {
    `kotlin-dsl`
    id("setup")
}

dependencies {
    compileOnly(projects.gradleWorker)
}

gradlePlugin.plugins.configureEach {
    displayName = "hfhbd mavencentral Gradle Plugin"
    description = "hfhbd mavencentral Gradle Plugin"
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
