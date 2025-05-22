package kapibarabanka.lo3.common
package lo3api

import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen}

object Lo3API:
  private val allClients: List[MyClient] = List(UserClient, FicDetailsClient, KindleClient, FicsClient, StatsClient)
  val openAPI: OpenAPI =
    OpenAPIGen.fromEndpoints(title = "Library Of Our Own API", version = "1.0", allClients.flatMap(_.allEndpoints))
