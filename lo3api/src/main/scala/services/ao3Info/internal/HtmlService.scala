package kapibarabanka.lo3.api
package services.ao3Info.internal

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.models.ao3.Ao3Url
import kapibarabanka.lo3.common.models.domain.{Lo3Error, NotFoundOnAo3, ParsingError}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import zio.*

import scala.language.postfixOps

class HtmlService(ao3: Ao3HttpClient):
  private val jsoupBrowser = JsoupBrowser()

  def workFromFile(id: String, fileName: String): ZIO[Any, Lo3Error, RestrictedWorkHtml] =
    val url        = Ao3Url.download(id, fileName)
    val entityName = s"work with id $id from url $url"
    for {
      body <- ao3.getFromFile(url, fileName)
      html <- ZIO.attempt(jsoupBrowser.parseString(body)).mapError(e => ParsingError(e.toString, entityName))
      doc <- ZIO.attempt(RestrictedWorkHtml(html, url)).mapError {
        case _: NoSuchElementException =>
          println(html.toString)
          ParsingError("Cannot parse html into custom doc: element not found exception", entityName)
        case e => ParsingError(e.toString, entityName)
      }
    } yield doc

  def work(id: String): IO[Lo3Error, WorkHtml] =
    getDoc(Ao3Url.work(id), s"work with id $id", PageType.Work)(html => WorkHtml(html))

  def seriesAllPages(id: String): IO[Lo3Error, List[SeriesPageHtml]] = for {
    entityName <- ZIO.succeed(s"series with id $id")
    firstPage  <- seriesFirstPage(id)
    allPages <- firstPage.pageCount match
      case None => ZIO.succeed(List(firstPage))
      case Some(pageCount) =>
        ZIO.collectAll(
          for i <- 1 to pageCount
          yield getDoc(Ao3Url.seriesPage(id, i), entityName, PageType.Series)(html => SeriesPageHtml(html, id, i))
        )
  } yield allPages.toList

  def seriesFirstPage(id: String): IO[Lo3Error, SeriesPageHtml] =
    getDoc(Ao3Url.series(id), s"series with id $id", PageType.Series)(html => SeriesPageHtml(html, id, 1))

  def tag(id: String): IO[Lo3Error, TagHtml] = getDoc(Ao3Url.tag(id), s"tag '$id'", PageType.Tag)(html => TagHtml(html))

  def tagExists(tag: String): IO[Lo3Error, Boolean] =
    ao3
      .get(Ao3Url.tag(tag), PageType.Tag)
      .map(_ => true)
      .catchSome({ case NotFoundOnAo3(_) => ZIO.succeed(false) })

  private def getDoc[TDoc](url: String, entityName: String, pageType: PageType)(htmlToDoc: Document => TDoc) =
    for {
      body <- ao3.get(url, pageType)
      html <- ZIO.attempt(jsoupBrowser.parseString(body)).mapError(e => ParsingError(e.toString, entityName))
      doc <- ZIO.attempt(htmlToDoc(html)).mapError {
        case _: NoSuchElementException =>
          println(html.toString)
          ParsingError("Cannot parse html into custom doc: element not found exception", entityName)
        case e => ParsingError(e.toString, entityName)
      }
    } yield doc
