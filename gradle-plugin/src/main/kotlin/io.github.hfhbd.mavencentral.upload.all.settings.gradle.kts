import io.github.hfhbd.mavencentral.gradle.MavenCentralAggregationExtension

gradle.lifecycle.beforeProject {
    if (this == rootProject) {
        pluginManager.apply("io.github.hfhbd.mavencentral.upload")
        val mavenCentral = extensions.getByName("mavenCentral") as MavenCentralAggregationExtension
        mavenCentral.dependencies {
            for(subproject in this@beforeProject.subprojects) {
                publishToMavenCentral.add(dependencyFactory.createProjectDependency(subproject.path))
            }
        }
    } else {
        pluginManager.apply("io.github.hfhbd.mavencentral")
    }
}
