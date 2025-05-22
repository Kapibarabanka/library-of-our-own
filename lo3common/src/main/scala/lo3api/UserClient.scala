package kapibarabanka.lo3.common
package lo3api

import models.domain.Lo3Error

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.PathCodec.string
import zio.http.endpoint.Endpoint

object UserClient extends MyClient:
  override protected val clientName = "user"

  val add = endpoint(Method.POST, string("userId"))
    .query(HttpCodec.query[String]("username").optional)
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)

  val allIds = endpoint(Method.GET, "allIds")
    .out[List[String]]
    .outError[Lo3Error](Status.InternalServerError)

  val setEmail = endpoint(Method.PATCH, string("userId") / "email")
    .query(HttpCodec.query[String]("email"))
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)

  val getEmail = endpoint(Method.GET, string("userId") / "email")
    .out[Option[String]]
    .outError[Lo3Error](Status.InternalServerError)
  
  val backlog = (endpoint(Method.GET, string("userId") / "backlog")
    .query(HttpCodec.query[Boolean]("needToLog"))
    .out[String]
    .outError[Lo3Error](Status.InternalServerError))

  override val allEndpoints = List(add, allIds, setEmail, getEmail, backlog)
