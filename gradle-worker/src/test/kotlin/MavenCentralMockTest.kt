package io.github.hfhbd.mavencentral.gradle

import io.github.hfhbd.mavencentral.gradle.workactions.configureMavenCentral
import io.github.hfhbd.mavencentral.gradle.workactions.uploadToMavenCentral
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.logging.Logger
import io.ktor.server.testing.*
import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.time.Duration

class MavenCentralMockTest {
    @Test
    fun success() = testApplication {
        application {
            mavenCentral()
        }
        val client = createClient {
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.SIMPLE
            }
            configureMavenCentral(
                userName = "user",
                password = "password",
            )
        }

        client.uploadToMavenCentral(
            zipFileName = "test.zip",
            zipFileSize = 0,
            zipFileStream = Buffer(),
            delay = Duration.ZERO,
        )
    }
}
