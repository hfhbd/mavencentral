plugins {
    id("com.gradle.develocity") version "3.18.1"
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
    }
}

rootProject.name = "mavencentral"

include(":runtime")
