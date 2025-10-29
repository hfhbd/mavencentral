plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
    id("io.github.hfhbd.mavencentral")
    id("java-test-fixtures")
}

kotlin.jvmToolchain(21)

testing.suites.withType(JvmTestSuite::class).configureEach {
    useKotlinTest()
}

java {
    withJavadocJar()
    withSourcesJar()
}

configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradleVersion.current().version)
        )
    }
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/mavencentral") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("hfhbd mavencentral")
            description.set("hfhbd mavencentral")
            url.set("https://github.com/hfhbd/mavencentral")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("hfhbd")
                    name.set("Philip Wedemann")
                    email.set("mybztg+mavencentral@icloud.com")
                }
            }
            scm {
                connection.set("scm:git://github.com/hfhbd/mavencentral.git")
                developerConnection.set("scm:git://github.com/hfhbd/mavencentral.git")
                url.set("https://github.com/hfhbd/mavencentral")
            }
            distributionManagement {
                repository {
                    id = "github"
                    name = "GitHub hfhbd Apache Maven Packages"
                    url = "https://maven.pkg.github.com/hfhbd/mavencentral"
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull,
    )
    sign(publishing.publications)
}
