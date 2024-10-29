package server

import UploadComponents
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.contentType
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.String

/**
 * Upload a deployment bundle intended to be published to Maven Central.
 */
public fun Route.uploadComponents(action: suspend ApplicationCall.(UploadComponents) -> String) {
  route(path = """/api/v1/publisher/upload""") {
    contentType(FormData) {
      post {
        val body = call.receive<UploadComponents>()
        val response = call.action(body)
        call.response.status(Created)
        call.respond(response)
      }
    }
  }
}
