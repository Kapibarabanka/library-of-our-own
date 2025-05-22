package kapibarabanka.lo3.common
package lo3api

import models.domain.Lo3Error

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.HttpCodec

object KindleClient extends MyClient:
  override protected val clientName = "kindle"

  val sendToKindle = (endpoint(Method.POST, "send-to-kindle")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey)
    .query(HttpCodec.query[Boolean]("needToLog"))

  val saveToFile = endpoint(Method.POST, "save-to-file")
    .query(HttpCodec.query[String]("ficId"))
    .query(HttpCodec.query[String]("ficType"))
    .out[String]
    .outError[Lo3Error](Status.InternalServerError)

  override val allEndpoints = List(sendToKindle, saveToFile)
