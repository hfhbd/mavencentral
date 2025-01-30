import io.ktor.http.content.PartData
import io.ktor.serialization.kotlinx.json.jsonIo
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.routing
import io.ktor.server.util.getValue
import kotlinx.serialization.json.JsonObject
import server.checkStatus
import server.uploadComponents
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

        var status = DeploymentResponseFilesDeploymentState.Pending
        checkStatus {
            val id: String by parameters
            assertEquals(DEPLOYMENT_ID, id)

            status = when (status) {
                DeploymentResponseFilesDeploymentState.Pending -> DeploymentResponseFilesDeploymentState.Validating
                DeploymentResponseFilesDeploymentState.Validating -> DeploymentResponseFilesDeploymentState.Validated
                DeploymentResponseFilesDeploymentState.Validated -> DeploymentResponseFilesDeploymentState.Published
                DeploymentResponseFilesDeploymentState.Publishing -> DeploymentResponseFilesDeploymentState.Published
                DeploymentResponseFilesDeploymentState.Published -> DeploymentResponseFilesDeploymentState.Published
                DeploymentResponseFilesDeploymentState.Failed -> DeploymentResponseFilesDeploymentState.Failed
            }

            CheckStatus(
                deploymentState = status,
                deploymentId = id,
                deploymentName = "Test deployment",
                cherryBomUrl = null,
                errors = JsonObject(emptyMap()),
                purls = emptyList(),
            )
        }
    }
}

private const val DEPLOYMENT_ID = "test-deployment-id"
