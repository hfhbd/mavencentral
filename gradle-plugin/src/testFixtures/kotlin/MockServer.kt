package io.github.hfhbd.mavencentral.gradle

import io.ktor.http.content.PartData
import io.ktor.serialization.kotlinx.json.jsonIo
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.routing
import io.ktor.server.util.getValue
import io.github.hfhbd.mavencentral.api.server.checkStatus
import io.github.hfhbd.mavencentral.api.server.uploadComponents
import io.github.hfhbd.mavencentral.api.DeploymentState
import io.github.hfhbd.mavencentral.api.CheckStatus
import kotlin.test.assertEquals

fun Application.mavenCentral() {
    install(ContentNegotiation) {
        jsonIo()
    }
    routing {
        uploadComponents {
            val publishingType: String by parameters
            assertEquals("AUTOMATIC", publishingType)
            val body = receiveMultipart()
            val bundle = (body.readPart() as PartData.FileItem)
            assertEquals("bundle", bundle.name)

            DEPLOYMENT_ID
        }

        var status = DeploymentState.Pending
        checkStatus {
            val id: String by parameters
            assertEquals(DEPLOYMENT_ID, id)

            status = when (status) {
                DeploymentState.Pending -> DeploymentState.Validating
                DeploymentState.Validating -> DeploymentState.Validated
                DeploymentState.Validated -> DeploymentState.Published
                DeploymentState.Publishing -> DeploymentState.Published
                DeploymentState.Published -> DeploymentState.Published
                DeploymentState.Failed -> DeploymentState.Failed
            }

            CheckStatus(
                deploymentState = status,
                deploymentId = id,
                deploymentName = "Test deployment",
                cherryBomUrl = null,
                errors = emptyMap(),
                purls = emptyList(),
            )
        }
    }
}

private const val DEPLOYMENT_ID = "test-deployment-id"
