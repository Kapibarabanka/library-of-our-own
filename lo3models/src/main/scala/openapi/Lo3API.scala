package kapibarabanka.lo3.models
package openapi

import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen}

object Lo3API:
  private val allClients: List[MyClient] = List(UserClient, FicDetailsClient, KindleClient, BotClient)
  val openAPI: OpenAPI =
    OpenAPIGen.fromEndpoints(title = "Library Of Our Own API", version = "1.0", allClients.flatMap(_.allEndpoints))
