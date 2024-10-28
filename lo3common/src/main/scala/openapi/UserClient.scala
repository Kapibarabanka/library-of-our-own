package kapibarabanka.lo3.common
package openapi

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
    |> withStringError

  val allIds = endpoint(Method.GET, "allIds")
    .out[List[String]]
    |> withStringError

  val setEmail = endpoint(Method.PATCH, string("userId") / "email")
    .query(HttpCodec.query[String]("email"))
    .out[Unit]
    |> withStringError

  val getEmail = endpoint(Method.GET, string("userId") / "email")
    .out[Option[String]]
    |> withStringError
  
  val backlog = (endpoint(Method.GET, string("userId") / "backlog")
    .query(HttpCodec.query[Boolean]("needToLog"))
    .out[String]
    |> withStringError)

  override val allEndpoints = List(add, allIds, setEmail, getEmail, backlog)
