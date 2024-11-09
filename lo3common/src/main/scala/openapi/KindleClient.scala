package kapibarabanka.lo3.common
package openapi

import scalaz.Scalaz.ToIdOps
import zio.http.Method
import zio.http.codec.HttpCodec

object KindleClient extends MyClient:
  override protected val clientName = "kindle"

  val sendToKindle = (endpoint(Method.POST, "send-to-kindle")
    .out[Unit]
    |> withStringError
    |> withKey)
    .query(HttpCodec.query[Boolean]("needToLog"))

  val saveToFile = endpoint(Method.POST, "save-to-file")
    .query(HttpCodec.query[String]("ficId"))
    .query(HttpCodec.query[String]("ficType"))
    .out[String]
    |> withStringError

  override val allEndpoints = List(sendToKindle, saveToFile)
