plugins {
    id("maven-publish")
    id("signing")
}

val mavenCentral = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentral.get())
}

dependencies {
    mavenCentral("io.ktor:ktor-client-java:3.0.0")
    mavenCentral("io.ktor:ktor-client-logging:3.0.0")
    mavenCentral("io.ktor:ktor-client-content-negotiation:3.0.0")
    mavenCentral("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
}

val publishToMavenCentral by tasks.registering {
    group = "publishing"
}

tasks.publish {
    dependsOn(publishToMavenCentral)
}

publishing {
    val localMavenCentralRepoDir = layout.buildDirectory.dir("mavencentral/${version}/repo")
    val repoName = "localMavenCentral"

    repositories.maven {
        name = repoName
        url = uri(localMavenCentralRepoDir)
    }

    publications.withType<MavenPublication>().all {
        val pubName = name.replaceFirstChar { it.uppercaseChar() }

        val publishToLocalMavenCentral = tasks.named(
            "publish${pubName}PublicationTo${repoName.replaceFirstChar { it.uppercaseChar() }}Repository",
            PublishToMavenRepository::class.java,
        ) {
            val signTaskName = "sign${pubName}Publication"
            if (signTaskName in tasks.names) {
                dependsOn(signTaskName)
            }
        }

        val repoFiles = files(localMavenCentralRepoDir) {
            builtBy(publishToLocalMavenCentral)
        }

        val createMavenCentralZipFile = tasks.register("createMavenCentralZipFile${pubName}", Zip::class) {
            archiveFileName.set("${project.group}-${project.name}-${version}.zip")
            from(repoFiles) {
                exclude {
                    it.name.startsWith("maven-metadata.xml")
                }
            }
            destinationDirectory.set(layout.buildDirectory.dir("mavencentral/publishing"))
        }

        val publishToMavenCentralTask = tasks.register(
            "publish${pubName}PublicationToMavenCentral",
            PublishToMavenCentral::class,
        ) {
            group = "publishing"
            uploadZip.set(createMavenCentralZipFile.flatMap {
                it.archiveFile
            })
            workerClassPath.from(mavenCentralWorkerClassPath)
        }

        publishToMavenCentral {
            dependsOn(publishToMavenCentralTask)
        }
    }
}
