package kapibarabanka.lo3.models
package openapi

import zio.http.endpoint.Endpoint

trait MyClient:
  protected val path: String
  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]]
