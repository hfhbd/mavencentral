import io.github.hfhbd.mavencentral.gradle.*

val deps = configurations.dependencyScope("mavenCentralClosing")

dependencies {
    deps("io.github.hfhbd.mavencentral:gradle-worker:$VERSION")
}

val classpath = configurations.resolvable("mavenCentralClosingClasspath") {
    extendsFrom(deps.get())
}

tasks.register("closeMavenCentral", CloseMavenCentral::class) {
    workerClassPath.from(classpath)
}
