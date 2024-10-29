package server

import CheckPublishedComponent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.server.routing.route

/**
 * Check whether a component is published.
 */
public fun Route.checkPublishedComponent(action: suspend ApplicationCall.() -> CheckPublishedComponent) {
  route(path = """/api/v1/publisher/published""") {
    `get` {
      val response = call.action()
      call.response.status(OK)
      call.respond(response)
    }
  }
}
