package io.github.hfhbd.mavencentral.gradle

import io.github.hfhbd.mavencentral.uploadToMavenCentralBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.logging.Logging as GradleLogging


abstract class PublishWorker : WorkAction<PublishWorker.PublishParameters> {
    interface PublishParameters : WorkParameters {
        val uploadZip: RegularFileProperty
        val username: Property<String>
        val password: Property<String>
    }

    private val gradleLogger = GradleLogging.getLogger(PublishWorker::class.java)

    override fun execute() {
        uploadToMavenCentralBlocking(
            username = parameters.username.get(),
            password = parameters.password.get(),
            zipBundle = parameters.uploadZip.get().asFile,
            isDebugEnabled = gradleLogger.isDebugEnabled,
        ) {
            if (gradleLogger.isDebugEnabled) {
                gradleLogger.debug(it)
            } else {
                gradleLogger.info(it)
            }
        }
    }
}
