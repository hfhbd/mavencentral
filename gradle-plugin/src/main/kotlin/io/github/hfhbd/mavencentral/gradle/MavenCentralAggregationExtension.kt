package io.github.hfhbd.mavencentral.gradle

import org.gradle.api.Action
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.tasks.Nested
import org.gradle.declarative.dsl.model.annotations.HiddenInDefinition

interface MavenCentralAggregationExtension {
    @get:Nested
    val dependencies: MavenCentralAggregationDependencies

    @HiddenInDefinition
    fun dependencies(action: Action<MavenCentralAggregationDependencies>) {
        action.execute(dependencies)
    }
}

interface MavenCentralAggregationDependencies : Dependencies {
    val publishToMavenCentral: DependencyCollector
}
