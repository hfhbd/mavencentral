package server

import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.server.routing.route
import kotlin.ByteArray

/**
 * Integrate deployment bundles with a build for manual testing. For more information, see the the following [documentation](https://central.sonatype.org/publish/publish-portal-api/#manually-testing-a-deployment-bundle).
 */
public fun Route.downloadDeployment(action: suspend ApplicationCall.() -> ByteArray) {
  route(path = """/api/v1/publisher/deployments/download/{relativePath}""") {
    `get` {
      val response = call.action()
      call.response.status(OK)
      call.respond(response)
    }
  }
}
