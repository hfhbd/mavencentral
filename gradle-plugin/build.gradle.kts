plugins {
    `kotlin-dsl`
    id("setup")
}

kotlin.jvmToolchain(21)

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(projects.centralApi)

    compileOnly(libs.ktor.client.java)
    compileOnly(libs.ktor.client.logging)

    testFixturesApi(testFixtures(projects.centralApi))
    testFixturesApi(libs.ktor.server.test.host)
}

testing.suites {
    withType(JvmTestSuite::class).configureEach {
        useKotlinTest()
    }
    named("test", JvmTestSuite::class) {
        dependencies {
            implementation(libs.ktor.client.logging)
        }
    }
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

val storeVersion by tasks.registering(StoreVersion::class) {
    version.put("centralApi", provider { project.group }.zip(provider { project.version }) { group, version ->
        "$group:central-api:$version"
    })
    version.put("ktorJava", libs.ktor.client.java.get().toString())
    version.put("ktorLogging", libs.ktor.client.logging.get().toString())
}

sourceSets.main {
    kotlin.srcDir(storeVersion)
}

gradlePlugin.plugins.configureEach {
    displayName = "hfhbd mavencentral Gradle Plugin"
    description = "hfhbd mavencentral Gradle Plugin"
}

configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradleVersion.current().version)
        )
    }
}
