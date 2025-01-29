import io.github.hfhbd.mavencentral.*

plugins {
    id("maven-publish")
    id("signing")
}

val mavenCentralWorker = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentralWorker.get())
}

dependencies {
    mavenCentralWorker(centralApi)
    mavenCentralWorker(ktorJava)
    mavenCentralWorker(ktorLogging)
}

val projectGroup = provider { group }
val projectName = provider { name }
val projectVersion = provider { version }

val localMavenCentralRepoDir = layout.buildDirectory.dir("mavencentral/$projectVersion/repo")
val repoFiles = files(localMavenCentralRepoDir)

val createMavenCentralZipFile = tasks.register("createMavenCentralZipFile", Zip::class) {
    archiveFileName.set(projectGroup.zip(projectName) { projectGroup, projectName ->
        "$projectGroup-$projectName"
    }.zip(projectVersion) { ga, projectVersion ->
        "$ga-$projectVersion.zip"
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

    publications.withType<MavenPublication>().all {
        val pubName = name.replaceFirstChar { it.uppercaseChar() }

        val publishToLocalMavenCentral = tasks.named(
            "publish${pubName}PublicationTo${repoName.replaceFirstChar { it.uppercaseChar() }}Repository",
        )
        repoFiles.builtBy(publishToLocalMavenCentral)
    }
}
