import io.github.hfhbd.mavencentral.gradle.*

plugins {
    id("maven-publish")
    id("signing")
}

val mavenCentralWorker = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentralWorker.get())
}

dependencies {
    mavenCentralWorker("io.github.hfhbd.mavencentral:gradle-worker:$VERSION")
}

val projectGroup = provider { group.toString() }
val projectName = name
val projectVersion = provider { version.toString() }

val localMavenCentralRepoDir = projectVersion.flatMap { layout.buildDirectory.dir("mavencentral/$it/repo") }
val repoFiles = files(localMavenCentralRepoDir)

val createMavenCentralZipFile = tasks.register("createMavenCentralZipFile", Zip::class) {
    archiveFileName.set(projectGroup.zip(projectVersion) { projectGroup, projectVersion ->
        "$projectGroup-$projectName-$projectVersion.zip"
    })
    from(repoFiles) {
        exclude {
            it.name.startsWith("maven-metadata.xml")
        }
    }
    destinationDirectory.set(layout.buildDirectory.dir("mavencentral/publishing"))
}

val publishToMavenCentral by tasks.registering(PublishToMavenCentral::class) {
    group = PublishingPlugin.PUBLISH_TASK_GROUP
    uploadZip.set(createMavenCentralZipFile.flatMap {
        it.archiveFile
    })
    workerClassPath.from(mavenCentralWorkerClassPath)
}

tasks.publish {
    dependsOn(publishToMavenCentral)
}

publishing {
    val repoName = "localMavenCentral"

    repositories.maven {
        name = repoName
        url = uri(localMavenCentralRepoDir)
    }

    val publishToLocalMavenCentral = publications.withType<MavenPublication>().map {
        val pubName = name.replaceFirstChar { it.uppercaseChar() }

        tasks.named(
            "publish${pubName}PublicationTo${repoName.replaceFirstChar { it.uppercaseChar() }}Repository",
        )
    }
    repoFiles.builtBy(publishToLocalMavenCentral)
}
