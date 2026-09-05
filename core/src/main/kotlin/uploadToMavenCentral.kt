package io.github.hfhbd.mavencentral

import io.github.hfhbd.mavencentral.api.DeploymentState
import io.github.hfhbd.mavencentral.api.PublishingTypePublishingType
import io.github.hfhbd.mavencentral.api.auth.BearerAuthAuth
import io.github.hfhbd.mavencentral.api.client.checkStatus
import io.github.hfhbd.mavencentral.api.client.uploadComponents
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.accept
import io.ktor.client.request.forms.*
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Plain
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.text.encodeToByteArray
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun uploadToMavenCentralBlocking(
    username: String,
    password: String,
    zipBundle: File,
    isDebugEnabled: Boolean,
    logger: (String) -> Unit,
) {
    val client = HttpClient(Java) {
        defaultRequest {
            url("https://central.sonatype.com")
        }
        configureMavenCentral(userName = username, password = password)

        install(Logging) {
            level = if (isDebugEnabled) {
                LogLevel.ALL
            } else {
                LogLevel.INFO
            }
            this.logger = object : Logger {
                override fun log(message: String) {
                    logger(message)
                }
            }
        }
    }

    runBlocking {
        client.uploadToMavenCentral(
            zipFileName = zipBundle.name,
            zipFileSize = zipBundle.length(),
            zipFileStream = zipBundle.inputStream().asSource(),
            delay = 1.seconds,
        )
    }
}

internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureMavenCentral(
    userName: String,
    password: String,
) {
    expectSuccess = true
    BearerAuthAuth(Base64.encode("$userName:$password".encodeToByteArray()))
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

            DeploymentState.Failed -> error(status.errors)
        }
    }
}
