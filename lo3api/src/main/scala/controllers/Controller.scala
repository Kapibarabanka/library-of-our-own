package kapibarabanka.lo3.api
package controllers

import zio.http.*
import zio.http.endpoint.Endpoint

protected[api] trait Controller:
  protected val path: String
  val routes: Routes[Any, Response]
  val endpoints: List[Endpoint[_, _, _, _, _]]
