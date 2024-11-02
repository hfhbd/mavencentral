plugins {
    id("java-library")
}

val s = configurations.dependencyScope("s")
val d = configurations.resolvable("fooo") {
    extendsFrom(s.get())
    resolutionStrategy {
        force("io.github.hfhbd.mavencentral:runtime:0.0.6")
        dependencySubstitution {
            substitute(module("io.github.hfhbd.mavencentral:runtime:0.0.6")).using(module("io.github.hfhbd.mavencentral:runtime:0.0.6"))
        }
    }
}

dependencies {
    s("io.github.hfhbd.mavencentral:central-api:0.0.6")
    s("io.github.hfhbd.mavencentral:runtime:0.0.6") {
        endorseStrictVersions()
        version {
            strictly("0.0.6")
            reject("0.0.10")
        }
        attributes {
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
        }
    }
}

repositories {
    mavenCentral()
}
