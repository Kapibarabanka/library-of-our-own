package kapibarabanka.lo3.models
package openapi

import zio.http.Method
import scalaz.Scalaz.ToIdOps

object BotClient extends MyClient:
  override protected val clientName = "bot"

  val sendToMe = endpoint(Method.POST, "send-to-me")
    .in[String]
    .out[Unit]
    |> withStringError

  val sendToAll = endpoint(Method.POST, "send-to-all")
    .in[String]
    .out[Unit]
    |> withStringError

  override val allEndpoints = List(sendToMe, sendToAll)
