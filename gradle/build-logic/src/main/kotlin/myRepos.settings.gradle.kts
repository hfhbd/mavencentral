plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("io.github.hfhbd.mavencentral.upload.all")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}
