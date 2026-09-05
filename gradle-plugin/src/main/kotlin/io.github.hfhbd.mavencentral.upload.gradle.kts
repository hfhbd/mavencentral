import io.github.hfhbd.mavencentral.gradle.*

val mavenCentralWorker = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentralWorker)
}

dependencies {
    mavenCentralWorker(core)
}

val extension = extensions.create<MavenCentralAggregationExtension>("mavenCentral")

val mavenCentralAggregation = configurations.resolvable("mavenCentralAggregation") {
    fromDependencyCollector(extension.dependencies.publishToMavenCentral)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, named("maven-central"))
    }
}

val projectGroup = provider { group.toString() }
val projectName = name
val projectVersion = provider { version.toString() }

val repoFiles = files(mavenCentralAggregation)

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
