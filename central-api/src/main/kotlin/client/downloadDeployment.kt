package client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.`get`
import kotlin.ByteArray
import kotlin.String
import kotlin.Unit

/**
 * Integrate deployment bundles with a build for manual testing. For more information, see the the following [documentation](https://central.sonatype.org/publish/publish-portal-api/#manually-testing-a-deployment-bundle).
 *
 * @param relativePath The full path to a specific file from a deployment bundle.
 */
public suspend fun HttpClient.downloadDeployment(relativePath: String, builder: suspend HttpRequestBuilder.() -> Unit = {}): ByteArray {
  val response = `get`(urlString = """api/v1/publisher/deployments/download/${relativePath}""") {
    builder()
  }
  return response.body<ByteArray>()
}
