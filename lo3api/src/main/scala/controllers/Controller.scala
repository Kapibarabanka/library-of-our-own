package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.models.openapi.MyClient
import zio.ZIO
import zio.http.*
import zio.http.endpoint.{Endpoint, EndpointMiddleware}

protected[api] trait Controller:
  val routes: List[Route[Any, Response]]
