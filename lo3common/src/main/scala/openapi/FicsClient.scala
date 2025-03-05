package kapibarabanka.lo3.common
package openapi

import models.api.{FicsPage, FicsPageRequest, HomePageData}
import models.domain.{Ao3FicInfo, Fic, FicCard, Lo3Error}

import scalaz.Scalaz.ToIdOps
import zio.http.Method.{GET, POST}
import zio.http.codec.*
import zio.http.codec.PathCodec.string
import zio.http.endpoint.Endpoint
import zio.http.*

object FicsClient extends MyClient:
  override protected val clientName: String = "fics"

  val getAllCards = endpoint(GET, string("userId") / "all-cards")
    .out[List[FicCard]]
    .outError[Lo3Error](Status.InternalServerError)

  val getFicByLink = endpoint(GET, "fic-by-link")
    .query(HttpCodec.query[String]("ficLink"))
    .query(HttpCodec.query[String]("userId"))
    .query(HttpCodec.query[Boolean]("needToLog"))
    .out[Fic]
    .outError[Lo3Error](Status.InternalServerError)

  val getFicByKey = endpoint(GET, "fic-by-key")
    .out[Fic]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val getHomePage = endpoint(GET, string("userId") / "home-page")
    .out[HomePageData]
    .outError[Lo3Error](Status.InternalServerError)

  val updateAo3Info = endpoint(POST, "update-ao3-info")
    .out[Ao3FicInfo]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  override val allEndpoints: List[Endpoint[_, _, _, _, _]] =
    List(getAllCards, getFicByLink, getFicByKey, getHomePage, updateAo3Info)
