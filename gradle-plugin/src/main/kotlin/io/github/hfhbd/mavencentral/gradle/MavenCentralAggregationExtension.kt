package io.github.hfhbd.mavencentral.gradle

import org.gradle.api.Action
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.tasks.Nested

interface MavenCentralAggregationExtension {
    @get:Nested
    val dependencies: MavenCentralAggregationDependencies

    fun dependencies(action: Action<MavenCentralAggregationDependencies>) {
        action.execute(dependencies)
    }
}

interface MavenCentralAggregationDependencies : Dependencies {
    val publishToMavenCentral: DependencyCollector
}
