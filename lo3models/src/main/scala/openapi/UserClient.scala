package kapibarabanka.lo3.models
package openapi

import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.PathCodec.string
import zio.http.endpoint.Endpoint
import zio.http.endpoint.EndpointMiddleware.None

object UserClient extends MyClient:
  override protected val path = "user"

  val add =
    Endpoint(RoutePattern.POST / path / string("userId"))
      .query(HttpCodec.query("username").optional)
      .out[Unit]
      .outError[String](Status.InternalServerError)

  val allIds = Endpoint(RoutePattern.GET / path / "allIds")
    .out[List[String]]
    .outError[String](Status.InternalServerError)

  val setEmail = Endpoint(RoutePattern.POST / path / string("userId") / "email")
    .query(HttpCodec.query("email"))
    .out[Unit]
    .outError[String](Status.InternalServerError)

  override val allEndpoints = List(add, allIds, setEmail)
