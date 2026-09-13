plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(21)

dependencies {
    implementation(projects.gradleTasks)
    compileOnly(projects.core)
}

val storeVersion = tasks.register("storeVersion",StoreVersion::class) {
    version.put("core", "io.github.hfhbd.mavencentral:core:${project.version}")
}

sourceSets.main {
    kotlin.srcDir(storeVersion)
}

gradlePlugin.plugins.configureEach {
    displayName = "hfhbd mavencentral Gradle Plugin"
    description = "hfhbd mavencentral Gradle Plugin"
}

tasks.validatePlugins {
    enableStricterValidation = true
}
