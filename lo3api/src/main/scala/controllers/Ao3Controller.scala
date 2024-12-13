package kapibarabanka.lo3.api
package controllers

import ao3scrapper.Ao3

import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType, NotAo3Link}
import kapibarabanka.lo3.common.openapi.Ao3Client
import zio.ZIO
import zio.http.*

case class Ao3Controller(ao3: Ao3) extends Controller:

  val workById   = Ao3Client.workById.implement { id => ao3.work(id)}
  val seriesById = Ao3Client.seriesById.implement { id => ao3.series(id) }

  val ficByLink = Ao3Client.ficByLink.implement { link =>
    Ao3Url.tryParseFicLink(link) match
      case None                     => ZIO.fail(NotAo3Link(link))
      case Some(id, FicType.Work)   => ao3.work(id).map(work => Right(work))
      case Some(id, FicType.Series) => ao3.series(id).map(series => Left(series))
  }

  val downloadLink = Ao3Client.downloadLink.implement { workId => ao3.downloadLink(workId) }

  override val routes: List[Route[Any, Response]] =
    List(workById, seriesById, ficByLink, downloadLink)
