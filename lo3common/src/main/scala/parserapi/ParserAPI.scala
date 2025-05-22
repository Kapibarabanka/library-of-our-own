package kapibarabanka.lo3.common
package parserapi

import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen}
object ParserAPI:
  private val allClients: List[MyClient] = List(ParserClient)
  val openAPI: OpenAPI =
    OpenAPIGen.fromEndpoints(title = "AO3 Parser API", version = "1.0", allClients.flatMap(_.allEndpoints))
