package kapibarabanka.lo3.models
package openapi

import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.PathCodec.string
import zio.http.endpoint.Endpoint

object UserClient extends MyClient:
  override protected val clientName = "user"

  val add = endpoint(Method.POST, string("userId"))
    .query(HttpCodec.query[String]("username").optional)
    .out[Unit]
    .outError[String](Status.InternalServerError)

  val allIds = endpoint(Method.GET, "allIds")
    .out[List[String]]
    .outError[String](Status.InternalServerError)

  val setEmail = endpoint(Method.PATCH, string("userId") / "email")
    .query(HttpCodec.query[String]("email"))
    .out[Unit]
    .outError[String](Status.InternalServerError)

  override val allEndpoints = List(add, allIds, setEmail)
