package io.github.hfhbd.mavencentral.gradle

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.util.encodeBase64
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import org.gradle.api.logging.Logging as GradleLogging
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault

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

internal abstract class CloseAction : WorkAction<CloseAction.Parameters> {
    interface Parameters : WorkParameters {
        val userName: Property<String>
        val password: Property<String>
        val namespace: Property<String>
    }

    private val gradleLogger = GradleLogging.getLogger(CloseAction::class.java)

    override fun execute() {
        val client = HttpClient(Java) {
            expectSuccess = true
            defaultRequest {
                url("https://ossrh-staging-api.central.sonatype.com")
                bearerAuth("${parameters.userName.get()}:${parameters.password.get()}".encodeBase64())
            }

            install(Logging) {
                level = if (gradleLogger.isDebugEnabled) {
                    LogLevel.ALL
                } else {
                    LogLevel.INFO
                }
                logger = object : Logger {
                    override fun log(message: String) {
                        if (gradleLogger.isDebugEnabled) {
                            gradleLogger.debug(message)
                        } else {
                            gradleLogger.info(message)
                        }
                    }
                }
            }
        }

        runBlocking {
            client.post("manual/upload/defaultRepository/${parameters.namespace.get()}") {
                parameter("publishing_type", "automatic")
            }
        }
    }
}
