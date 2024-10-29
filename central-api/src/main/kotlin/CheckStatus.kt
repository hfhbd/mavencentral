import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CheckStatus(
    val deploymentId: String,
    val deploymentName: String,
    val deploymentState: DeploymentResponseFilesDeploymentState,
    val purls: List<String>,
    val errors: JsonObject,
    val cherryBomUrl: String? = null,
)
