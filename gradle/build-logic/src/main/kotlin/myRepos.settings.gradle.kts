plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("com.gradle.develocity")
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        val isCI = providers.environmentVariable("CI").isPresent
        publishing {
            onlyIf { isCI }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}
