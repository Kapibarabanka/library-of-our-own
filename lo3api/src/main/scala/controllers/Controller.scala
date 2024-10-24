package kapibarabanka.lo3.api
package controllers

import zio.ZIO
import zio.http.*
import zio.http.endpoint.{Endpoint, EndpointMiddleware}

protected[api] trait Controller:
  protected val path: String
  protected def create[PathInput, Input, Err, Output, Middleware <: EndpointMiddleware](
      endpoint: Endpoint[PathInput, Input, Err, Output, Middleware]
  )(f: Input => ZIO[Any, Err, Output]): (Endpoint[PathInput, Input, Err, Output, Middleware], Route[Any, Response]) =
    (endpoint, endpoint.implement(f))
  val routes: List[Route[Any, Response]]
  val endpoints: List[Endpoint[_, _, _, _, _]]
