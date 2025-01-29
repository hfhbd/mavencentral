package io.github.hfhbd.mavencentral

import DeploymentResponseFilesDeploymentState
import auth.BearerAuthAuth
import client.checkStatus
import client.uploadComponents
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.ContentType.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.RawSource
import kotlinx.io.asSource
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.logging.Logging as GradleLogging

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

    override fun execute() {
        val client = HttpClient(Java) {
            defaultRequest {
                url("https://central.sonatype.com")
            }
            configureMavenCentral(userName = parameters.userName.get(), password = parameters.password.get())

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
        val uploadFile = parameters.uploadZip.asFile.get()
        runBlocking {
            client.uploadToMavenCentral(
                zipFileName = uploadFile.name,
                zipFileSize = uploadFile.length(),
                zipFileStream = uploadFile.inputStream().asSource(),
                delay = 1.seconds,
            )
        }
    }
}

internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureMavenCentral(
    userName: String,
    password: String,
) {
    expectSuccess = true
    BearerAuthAuth("$userName:$password".encodeBase64())
    install(ContentNegotiation) {
        jsonIo()
    }
}

internal suspend fun HttpClient.uploadToMavenCentral(
    zipFileName: String,
    zipFileSize: Long,
    zipFileStream: RawSource,
    delay: Duration,
) {
    val deploymentId = uploadComponents(
        publishingType = PublishingTypePublishingType.Automatic,
    ) {
        setBody(MultiPartFormDataContent(formData {
            append(
                key = "bundle",
                filename = zipFileName,
                contentType = Application.OctetStream,
                size = zipFileSize,
            ) {
                transferFrom(zipFileStream)
            }
        }))
    }
    while (true) {
        delay(delay)
        val status = checkStatus(id = deploymentId)!!
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
