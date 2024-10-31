package io.github.hfhbd.mavencentral

import client.checkStatus
import client.uploadComponents
import io.ktor.client.*
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
import kotlinx.io.asSource
import java.io.File
import kotlin.time.Duration.Companion.seconds

suspend fun uploadZipFileToMavenCentral(
    zipFile: File,
    userName: String,
    password: String,
    customLogger: (String) -> Unit,
) {
    val client = HttpClient(Java) {
        expectSuccess = true
        defaultRequest {
            url("https://central.sonatype.com")

            bearerAuth("$userName:$password".encodeBase64())
        }
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    customLogger(message)
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
            DeploymentResponseFilesDeploymentState.Validated,
                -> continue

            DeploymentResponseFilesDeploymentState.Publishing,
            DeploymentResponseFilesDeploymentState.Published,
                -> break

            DeploymentResponseFilesDeploymentState.Failed -> error(status.errors)
        }
    }
}
