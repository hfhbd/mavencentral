package io.github.hfhbd.mavencentral

import DeploymentResponseFilesDeploymentState
import auth.BearerAuthAuth
import client.checkStatus
import client.uploadComponents
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSource
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging as GradleLogging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@DisableCachingByDefault
abstract class PublishToMavenCentral : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val uploadZip: RegularFileProperty

    @get:Input
    internal val credentials: Provider<PasswordCredentials> =
        project.providers.credentials(PasswordCredentials::class, "mavenCentral")

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClassPath: ConfigurableFileCollection

    @TaskAction
    internal fun publish() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClassPath)
        }.submit(PublishWorker::class.java) {
            this.uploadZip.set(this@PublishToMavenCentral.uploadZip)
            this.userName.set(this@PublishToMavenCentral.credentials.map { it.username })
            this.password.set(this@PublishToMavenCentral.credentials.map { it.password })
        }
    }
}

internal abstract class PublishWorker : WorkAction<PublishWorker.PublishParameters> {
    interface PublishParameters : WorkParameters {
        val uploadZip: RegularFileProperty
        val userName: Property<String>
        val password: Property<String>
    }

    private val gradleLogger = GradleLogging.getLogger(PublishWorker::class.java)

    override fun execute(): Unit = runBlocking {
        val zipFile = this@PublishWorker.parameters.uploadZip.asFile.get()
        val client = HttpClient(Java) {
            expectSuccess = true
            defaultRequest {
                url("https://central.sonatype.com")
            }
            BearerAuthAuth("${parameters.userName.get()}:${parameters.password.get()}".encodeBase64())
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = if (gradleLogger.isDebugEnabled) {
                    LogLevel.ALL
                } else {
                    LogLevel.INFO
                }
                this.logger = object : Logger {
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
        val deploymentId = client.uploadComponents {
            setBody(MultiPartFormDataContent(formData {
                append(
                    key = "bundle",
                    filename = zipFile.name,
                    contentType = Application.OctetStream,
                    size = zipFile.length(),
                ) {
                    transferFrom(zipFile.inputStream().asSource())
                }
            }))
        }
        while (true) {
            delay(1.seconds)
            val status = client.checkStatus(id = deploymentId)!!
            when (status.deploymentState) {
                DeploymentResponseFilesDeploymentState.Pending,
                DeploymentResponseFilesDeploymentState.Validating,
                    -> continue

                DeploymentResponseFilesDeploymentState.Validated,
                DeploymentResponseFilesDeploymentState.Publishing,
                DeploymentResponseFilesDeploymentState.Published,
                    -> break

                DeploymentResponseFilesDeploymentState.Failed -> error(status.errors)
            }
        }
    }
}
