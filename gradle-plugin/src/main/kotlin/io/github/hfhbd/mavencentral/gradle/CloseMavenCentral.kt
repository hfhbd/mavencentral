package io.github.hfhbd.mavencentral.gradle

import io.github.hfhbd.mavencentral.gradle.workactions.CloseAction
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@DisableCachingByDefault(because = "Remote operation")
abstract class CloseMavenCentral : DefaultTask() {
    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClassPath: ConfigurableFileCollection

    @get:Input
    internal val credentials: Provider<PasswordCredentials> =
        project.providers.credentials(PasswordCredentials::class, "mavenCentralStaging")

    @get:Input
    abstract val namespace: Property<String>

    @TaskAction
    internal fun close() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClassPath)
        }.submit(CloseAction::class.java) {
            userName.set(credentials.map { it.username })
            password.set(credentials.map { it.password })
            namespace.set(this@CloseMavenCentral.namespace)
        }
    }
}
