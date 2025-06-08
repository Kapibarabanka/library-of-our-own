package kapibarabanka.lo3.api
package services.ao3Info

import services.ao3Info.internal.{Ao3HttpClient, Ao3HttpClientImpl, HtmlService, TagService, WorkDoc}
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3
import kapibarabanka.lo3.common.models.ao3.*
import kapibarabanka.lo3.common.models.domain.{Ao3FicInfo, DownloadLinkNotFound, Lo3Error}
import kapibarabanka.lo3.common.services.{EmptyLog, OptionalLog}
import zio.{IO, ZIO, ZLayer}

trait Ao3InfoService:
  def htmlService: HtmlService
  def getAo3Info(
      id: String,
      ficType: FicType,
      htmlFileName: Option[String] = None,
      log: OptionalLog = EmptyLog()
  ): IO[Lo3Error, Ao3FicInfo]
  def updateAo3Info(id: String, ficType: FicType, log: OptionalLog = EmptyLog()): IO[Lo3Error, Ao3FicInfo]
  def downloadLink(workId: String): IO[Lo3Error, String]
  def seriesWorks(seriesId: String): IO[Lo3Error, List[String]]
  def ficName(id: String, ficType: FicType): IO[Lo3Error, String]

case class Ao3InfoServiceImpl(ao3: Ao3HttpClient) extends Ao3InfoService:
  val htmlService        = HtmlService(ao3)
  private val tagService = TagService(htmlService)

  override def ficName(id: String, ficType: FicType): IO[Lo3Error, String] = ficType match
    case FicType.Work   => htmlService.work(id).map(doc => doc.title)
    case FicType.Series => htmlService.seriesFirstPage(id).map(doc => doc.title)

  override def seriesWorks(seriesId: String): IO[Lo3Error, List[String]] = for {
    seriesExists <- Lo3Data.series.exists(seriesId)
    workIds <-
      if (seriesExists) Lo3Data.series.workIds(seriesId)
      else htmlService.seriesAllPages(seriesId).map(pages => pages.flatMap(page => page.works).map(_.id))
  } yield workIds

  override def downloadLink(workId: String): IO[Lo3Error, String] = for {
    work <- getWork(workId, EmptyLog(), None)
    link <- work.downloadLink match {
      case Some(value) => ZIO.succeed(value)
      case None =>
        htmlService
          .work(workId)
          .flatMap(doc => doc.mobiLink.fold[IO[Lo3Error, String]](ZIO.fail(DownloadLinkNotFound(workId)))(s => ZIO.succeed(s)))
          .map(Ao3Url.download)
    }
  } yield link

  override def updateAo3Info(id: String, ficType: FicType, log: OptionalLog): IO[Lo3Error, Ao3FicInfo] = ficType match
    case FicType.Work =>
      for {
        updatedWork <- parseWork(id, log, None)
        updatedFic  <- Lo3Data.works.updateWork(updatedWork)
      } yield updatedFic
    case FicType.Series =>
      for {
        updatedSeries <- parseSeries(id, log)
        updatedFic    <- Lo3Data.series.update(updatedSeries)
      } yield updatedFic

  override def getAo3Info(
      id: String,
      ficType: FicType,
      htmlFileName: Option[String] = None,
      log: OptionalLog = EmptyLog()
  ): IO[Lo3Error, Ao3FicInfo] = ficType match
    case FicType.Work   => getWork(id, log, htmlFileName)
    case FicType.Series => getSeries(id, log)

  private def getWork(id: String, log: OptionalLog, htmlFileName: Option[String]): IO[Lo3Error, Ao3FicInfo] = for {
    maybeFic <- Lo3Data.works.getById(id)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None =>
        for {
          work    <- parseWork(id, log, htmlFileName)
          _       <- log.edit(s"Work parsed, saving to database...")
          flatFic <- Lo3Data.works.add(work)
        } yield flatFic
  } yield fic

  private def parseWork(id: String, log: OptionalLog, htmlFileName: Option[String]): IO[Lo3Error, Work] = for {
    _ <- log.edit(s"Parsing work with id '$id'...")
    doc: WorkDoc <- htmlFileName match {
      case Some(fileName) => htmlService.workFromFile(id, fileName)
      case None           => htmlService.work(id)
    }
    _             <- log.edit(s"Parsing fandoms...")
    fandoms       <- ZIO.collectAll(doc.fandoms.map(tagService.fandom))
    _             <- log.edit(s"Parsing characters...")
    characters    <- ZIO.collectAll(doc.characters.map(tagService.character))
    _             <- log.edit(s"Parsing relationships...")
    relationships <- ZIO.collectAll(doc.relationships.map(tagService.relationship(_)))
    _             <- log.edit(s"Parsing tags...")
    freeformTags  <- ZIO.collectAll(doc.freeformTags.map(tagService.freeformTag))
    work <- ZIO.succeed(
      Work(
        id = id,
        title = doc.title,
        authors = doc.authors.toList,
        rating = Rating.withName(doc.rating),
        warnings = getWarnings(doc.warnings),
        categories = doc.categories.map(Category.withName).toSet,
        fandoms = fandoms.toSet,
        relationships = relationships.filter(r => r.isInstanceOf[Relationship]).map(_.asInstanceOf[Relationship]),
        parsedShips = relationships.filter(r => r.isInstanceOf[String]).map(_.asInstanceOf[String]),
        characters = characters.toSet,
        freeformTags = freeformTags.distinct,
        link = Ao3Url.work(id),
        date = {
          doc.updated match
            case Some(date) => PublishedAndUpdated(doc.published, date)
            case None       => Published(doc.published)
        },
        words = doc.words,
        chaptersWritten = doc.chaptersWritten.get,
        chaptersPlanned = doc.chaptersPlanned,
        downloadLink = doc.mobiLink.flatMap(l => Ao3Url.cleanDownloadUrl(l))
      )
    )
    _ <- ZIO.log(s"Parsed work: $work")
  } yield work

  def getSeries(id: String, log: OptionalLog): IO[Lo3Error, Ao3FicInfo] = for {
    maybeFic <- Lo3Data.series.getById(id)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None =>
        for {
          series  <- parseSeries(id, log)
          _       <- log.edit(s"Series parsed, saving to database...")
          flatFic <- Lo3Data.series.add(series)
        } yield flatFic
  } yield fic

  private def parseSeries(id: String, log: OptionalLog) = for {
    _           <- log.edit(s"Parsing series with id '$id'...")
    pageDocs    <- htmlService.seriesAllPages(id)
    allWorkDocs <- ZIO.succeed(pageDocs.flatMap(_.works))
    newWorkDocs <- ZIO
      .collectAll(allWorkDocs.map(wd => Lo3Data.works.exists(wd.id).map(exists => if (exists) None else Some(wd))))
      .map(_.flatten)
    _               <- log.edit(s"Parsing fandoms...")
    allFandoms      <- tagService.canonize(newWorkDocs.flatMap(_.fandoms).distinct)(tagService.fandom)
    _               <- log.edit(s"Parsing tags...")
    allFreeformTags <- tagService.canonize(newWorkDocs.flatMap(_.freeformTags).distinct)(tagService.freeformTag)
    _               <- log.edit(s"Parsing characters...")
    allCharacters   <- tagService.canonize(newWorkDocs.flatMap(_.characters).distinct)(tagService.character)
    _               <- log.edit(s"Parsing relationships...")
    allShips <- tagService
      .canonize(newWorkDocs.flatMap(_.relationships).distinct)(tagService.relationship(_: String))
    works <- ZIO.succeed(
      newWorkDocs
        .map(doc =>
          Work(
            id = doc.id,
            title = doc.title,
            authors = doc.authors,
            rating = Rating.withName(doc.rating),
            warnings = getWarnings(doc.warnings),
            categories = doc.categories.map(Category.withName).toSet,
            fandoms = doc.fandoms.map(allFandoms(_)).toSet,
            relationships = doc.relationships
              .map(allShips(_))
              .filter(r => r.isInstanceOf[Relationship])
              .map(_.asInstanceOf[Relationship])
              .toList,
            parsedShips =
              doc.relationships.map(allShips(_)).filter(r => r.isInstanceOf[String]).map(_.asInstanceOf[String]).toList,
            characters = doc.characters.map(allCharacters(_)).toSet,
            freeformTags = doc.freeformTags.map(allFreeformTags(_)).toList,
            link = Ao3Url.work(id),
            date = SingleDate(doc.stats.date),
            words = doc.stats.words,
            chaptersWritten = doc.stats.chaptersWritten.get,
            chaptersPlanned = doc.stats.chaptersPlanned,
            downloadLink = None
          )
        )
    )
    firstPage <- ZIO.succeed(pageDocs.head)
    series <- ZIO.succeed(
      Series(
        id = id,
        link = Ao3Url.series(id),
        title = firstPage.title,
        authors = firstPage.authors.toList,
        started = firstPage.begun.get,
        updated = firstPage.updated,
        words = firstPage.words.get,
        complete = firstPage.complete.get,
        bookmarks = firstPage.bookmarks,
        description = firstPage.description,
        unsavedWorks = works,
        workIds = allWorkDocs.sortBy(_.indexInSeries).map(_.id)
      )
    )
    _ <- ZIO.log(s"Parsed series: $series")
  } yield series

  private def getWarnings(warnings: Seq[String]): Set[ArchiveWarning] = warnings match {
    case Seq("No Archive Warnings Apply") => Set()
    case otherWarnings                    => otherWarnings.map(ArchiveWarning(_)).toSet
  }

object Ao3InfoService:
  private val ownLayer = ZLayer {
    ZIO.service[Ao3HttpClient].map(Ao3InfoServiceImpl(_))
  }

  def live(login: String, password: String): ZLayer[Any, Throwable, Ao3InfoService] =
    ZLayer.make[Ao3InfoService](ownLayer, Ao3HttpClientImpl.layer(login, password))
