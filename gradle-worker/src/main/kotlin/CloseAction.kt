package io.github.hfhbd.mavencentral.gradle.workactions

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.logging.Logging as GradleLogging

abstract class CloseAction : WorkAction<CloseAction.Parameters> {
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
