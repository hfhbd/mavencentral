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

val storeVersion by tasks.registering(StoreVersion::class)

sourceSets.main {
    kotlin.srcDir(storeVersion)
}
