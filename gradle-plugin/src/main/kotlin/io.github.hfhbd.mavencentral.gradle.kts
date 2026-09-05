plugins {
    id("maven-publish")
    id("signing")
}

val localMavenCentralRepoDir = layout.buildDirectory.dir("mavencentral/repo")
val repoFiles = files(localMavenCentralRepoDir)

val mavenCentralArtifacts = configurations.consumable("mavenCentralArtifacts") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, named("maven-central"))
    }
    outgoing.artifact(localMavenCentralRepoDir) {
        builtBy(repoFiles.buildDependencies)
    }
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
