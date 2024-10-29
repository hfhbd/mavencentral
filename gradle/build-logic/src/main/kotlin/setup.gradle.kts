plugins {
    id("java-test-fixtures")
    id("maven-publish")
    id("signing")
    id("mavenCentral")
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/adventOfCode") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("hfhbd AdventOfCode")
            description.set("hfhbd AdventOfCode")
            url.set("https://github.com/hfhbd/adventOfCode")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
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
                connection.set("scm:git://github.com/hfhbd/adventOfCode.git")
                developerConnection.set("scm:git://github.com/hfhbd/adventOfCode.git")
                url.set("https://github.com/hfhbd/adventOfCode")
            }

            // https://github.com/gradle/gradle/issues/28759
            this.withXml {
                this.asNode().appendNode("distributionManagement").appendNode("repository").apply {
                    this.appendNode("id", "github")
                    this.appendNode("name", "GitHub hfhbd Apache Maven Packages")
                    this.appendNode("url", "https://maven.pkg.github.com/hfhbd/adventOfCode")
                }
            }
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingKey")
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), providers.gradleProperty("signingPassword").get())
        sign(publishing.publications)
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions {}
    dirPermissions {}
}

configurations.consumable("githubPublications") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("GITHUB_OUTPUT"))
    }
    outgoing {
        artifacts(provider {
            publishing.publications.withType<MavenPublication>().flatMap {
                it.artifacts
            }.map {
                it.file
            }
        })
    }
}
