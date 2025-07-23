import io.github.hfhbd.mavencentral.gradle.ktorJava
import io.github.hfhbd.mavencentral.gradle.ktorLogging

val deps = configurations.dependencyScope("mavenCentralClosing")

dependencies {
    deps(ktorJava)
    deps(ktorLogging)
}

val classpath = configurations.resolvable("mavenCentralClosingClasspath") {
    extendsFrom(deps.get())
}

tasks.register("closeMavenCentral", io.github.hfhbd.mavencentral.gradle.CloseMavenCentral::class) {
    workerClassPath.from(classpath)
}
