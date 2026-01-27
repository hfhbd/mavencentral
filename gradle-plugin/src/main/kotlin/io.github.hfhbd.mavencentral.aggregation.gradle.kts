import io.github.hfhbd.mavencentral.gradle.*

val mavenCentralWorker = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath") {
    extendsFrom(mavenCentralWorker.get())
}

dependencies {
    mavenCentralWorker(ktorJava)
    mavenCentralWorker(ktorLogging)
    mavenCentralWorker(ktorClientContentNegotiation)
    mavenCentralWorker(ktorSerializationKotlinxJson)
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

val publishToMavenCentral by tasks.registering(PublishToMavenCentral::class) {
    group = PublishingPlugin.PUBLISH_TASK_GROUP
    uploadZip.set(createMavenCentralZipFile.flatMap {
        it.archiveFile
    })
    workerClassPath.from(mavenCentralWorkerClassPath)
}
