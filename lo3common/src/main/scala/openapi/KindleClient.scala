package kapibarabanka.lo3.common
package openapi

import scalaz.Scalaz.ToIdOps
import zio.http.Method

object KindleClient extends MyClient:
  override protected val clientName = "kindle"

  val sendToKindle = endpoint(Method.POST, "send-to-kindle")
    .out[Unit]
    |> withStringError
    |> withKey

  val saveToFile = endpoint(Method.POST, "save-to-file")
    .out[String]
    |> withStringError
    |> withKey

  override val allEndpoints = List(sendToKindle, saveToFile)
