package server

import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.server.routing.route
import kotlin.ByteArray

/**
 * Integrate a deployment bundle with a build for manual testing. For more information, see the the following [documentation](https://central.sonatype.org/publish/publish-portal-api/#manually-testing-a-deployment-bundle).
 */
public fun Route.getDeployment(action: suspend ApplicationCall.() -> ByteArray) {
  route(path = """/api/v1/publisher/deployment/{deploymentId}/download/{relativePath}""") {
    `get` {
      val response = call.action()
      call.response.status(OK)
      call.respond(response)
    }
  }
}
