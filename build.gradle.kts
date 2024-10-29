val publications = configurations.dependencyScope("publications")
val subPublicationFiles = configurations.resolvable("publicationFiles") {
    extendsFrom(publications.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("GITHUB_OUTPUT"))
    }
}

dependencies {
    for (p in subprojects) {
        publications(p)
    }
}

tasks.register("writePublicationsToGithubOutputFile", WritePublicationsToGitHubOutputFile::class) {
    publicationFiles.from(subPublicationFiles)
    rootDirectory.set(layout.projectDirectory)
    githubOutputFile.set(file(providers.environmentVariable("GITHUB_OUTPUT")))
}
