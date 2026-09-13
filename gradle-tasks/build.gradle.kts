plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(21)

dependencies {
    compileOnly(projects.core)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
