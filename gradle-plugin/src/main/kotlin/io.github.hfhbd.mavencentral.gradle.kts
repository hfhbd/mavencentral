import io.github.hfhbd.mavencentral.gradle.*

plugins {
    id("maven-publish")
    id("signing")
}

val mavenCentralWorker = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentralWorker)
}

dependencies {
    mavenCentralWorker(core)
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

val publishToMavenCentral = tasks.register("publishToMavenCentral", PublishToMavenCentral::class) {
    group = PublishingPlugin.PUBLISH_TASK_GROUP
    uploadZip.set(createMavenCentralZipFile.flatMap {
        it.archiveFile
    })
    workerClassPath.from(mavenCentralWorkerClassPath)
}

tasks.publish {
    dependsOn(publishToMavenCentral)
}

val mavenCentralArtifacts = configurations.consumable("mavenCentralArtifacts") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, named("maven-central"))
    }
    outgoing.artifact(localMavenCentralRepoDir)
}

publishing {
    val repoName = "localMavenCentral"

    repositories.maven {
        name = repoName
        url = uri(localMavenCentralRepoDir)
    }

    publications.withType<MavenPublication>().all {
        val pubName = name.replaceFirstChar { it.uppercaseChar() }

        val publishToLocalMavenCentral = tasks.named(
            "publish${pubName}PublicationTo${repoName.replaceFirstChar { it.uppercaseChar() }}Repository",
            PublishToMavenRepository::class,
        )
        repoFiles.builtBy(publishToLocalMavenCentral)
    }
}
