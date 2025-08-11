package io.github.hfhbd.mavencentral.gradle.workactions

import io.github.hfhbd.mavencentral.api.DeploymentState
import io.github.hfhbd.mavencentral.api.PublishingTypePublishingType
import io.github.hfhbd.mavencentral.api.auth.BearerAuthAuth
import io.github.hfhbd.mavencentral.api.client.checkStatus
import io.github.hfhbd.mavencentral.api.client.uploadComponents
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Plain
import io.ktor.serialization.kotlinx.json.jsonIo
import io.ktor.util.encodeBase64
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.RawSource
import kotlinx.io.asSource
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.logging.Logging as GradleLogging

abstract class PublishWorker : WorkAction<PublishWorker.PublishParameters> {
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
                contentType = ContentType.Application.OctetStream,
                size = zipFileSize,
            ) {
                transferFrom(zipFileStream)
            }
        }))
        accept(Plain)
    }
    while (true) {
        delay(delay)
        val status = checkStatus(id = deploymentId)!!
        when (status.deploymentState) {
            DeploymentState.Pending,
            DeploymentState.Validating,
                -> continue

            DeploymentState.Validated,
            DeploymentState.Publishing,
            DeploymentState.Published,
                -> break

            DeploymentState.Failed -> error(status.errors!!)
        }
    }
}
