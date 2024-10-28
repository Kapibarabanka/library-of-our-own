package kapibarabanka.lo3.api
package controllers

import ao3scrapper.Ao3

import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType}
import kapibarabanka.lo3.common.openapi.Ao3Client
import zio.ZIO
import zio.http.*

case class Ao3Controller(ao3: Ao3) extends Controller:

  val workById   = Ao3Client.workById.implement { id => ao3.work(id).mapError(e => e.getMessage) }
  val seriesById = Ao3Client.seriesById.implement { id => ao3.series(id).mapError(e => e.getMessage) }

  val ficByLink = Ao3Client.ficByLink.implement { link =>
    Ao3Url.tryParseFicLink(link) match
      case None                     => ZIO.fail("Not a parsable Ao3 link")
      case Some(id, FicType.Work)   => ao3.work(id).mapError(e => e.getMessage).map(work => Right(work))
      case Some(id, FicType.Series) => ao3.series(id).mapError(e => e.getMessage).map(series => Left(series))
  }

  val downloadLink = Ao3Client.downloadLink.implement { workId => ao3.downloadLink(workId).mapError(e => e.getMessage) }

  override val routes: List[Route[Any, Response]] =
    List(workById, seriesById, ficByLink, downloadLink)
