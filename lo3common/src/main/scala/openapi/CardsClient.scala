package kapibarabanka.lo3.common
package openapi

import models.api.{FicsPage, FicsPageRequest}
import models.domain.Lo3Error
import openapi.FicDetailsClient.endpoint

import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

object CardsClient extends MyClient:
  override protected val clientName: String = "cards"

  val getFicsPage = endpoint(Method.POST, "fics-page")
    .in[FicsPageRequest]
    .out[FicsPage]
    .outError[Lo3Error](Status.InternalServerError)

  override val allEndpoints: List[Endpoint[_, _, _, _, _]] = List(getFicsPage)
