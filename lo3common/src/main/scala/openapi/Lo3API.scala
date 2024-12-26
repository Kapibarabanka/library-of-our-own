package kapibarabanka.lo3.common
package openapi

import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen}

object Lo3API:
  private val allClients: List[MyClient] = List(CardsClient, UserClient, FicDetailsClient, KindleClient, BotClient, Ao3Client)
  val openAPI: OpenAPI =
    OpenAPIGen.fromEndpoints(title = "Library Of Our Own API", version = "1.0", allClients.flatMap(_.allEndpoints))
