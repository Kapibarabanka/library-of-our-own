package kapibarabanka.lo3.api
package ficService

import ficService.internal.*

import kapibarabanka.lo3.common.models.ao3
import kapibarabanka.lo3.common.models.ao3.*
import kapibarabanka.lo3.common.models.domain.{DownloadLinkNotFound, FlatFicModel, Lo3Error}
import kapibarabanka.lo3.common.services.{EmptyLog, OptionalLog}
import zio.{IO, ZIO, ZLayer}

trait FicService:
  def getFic(id: String, ficType: FicType, log: OptionalLog = EmptyLog()): IO[Lo3Error, FlatFicModel]
  def downloadLink(workId: String): IO[Lo3Error, String]
  def seriesWorks(seriesId: String): IO[Lo3Error, List[String]]
  def ficName(id: String, ficType: FicType): IO[Lo3Error, String]

case class FicServiceImpl(ao3: Ao3HttpClient) extends FicService:
  private val htmlService = HtmlService(ao3)
  private val tagService  = TagService(htmlService)

  override def ficName(id: String, ficType: FicType): IO[Lo3Error, String] = ficType match
    case FicType.Work   => htmlService.work(id).map(doc => doc.title)
    case FicType.Series => htmlService.seriesFirstPage(id).map(doc => doc.title)

  override def seriesWorks(seriesId: String): IO[Lo3Error, List[String]] = for {
    seriesExists <- data.series.exists(seriesId)
    workIds <-
      if (seriesExists) data.series.workIds(seriesId)
      else htmlService.seriesAllPages(seriesId).map(pages => pages.flatMap(page => page.works).map(_.id))
  } yield workIds

  override def downloadLink(workId: String): IO[Lo3Error, String] = for {
    doc  <- htmlService.work(workId)
    link <- doc.mobiLink.fold[IO[Lo3Error, String]](ZIO.fail(DownloadLinkNotFound(workId)))(s => ZIO.succeed(s))
  } yield Ao3Url.download(link)

  override def getFic(id: String, ficType: FicType, log: OptionalLog = EmptyLog()): IO[Lo3Error, FlatFicModel] = ficType match
    case FicType.Work => getWork(id, log)
    case FicType.Series => getSeries(id, log)

  private def getWork(id: String, log: OptionalLog): IO[Lo3Error, FlatFicModel] = for {
    maybeFic <- data.works.getById(id)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None      => parseAndSaveWork(id, log)
  } yield fic

  private def parseAndSaveWork(id: String, log: OptionalLog): IO[Lo3Error, FlatFicModel] = for {
    _             <- log.edit(s"Parsing work with id '$id'...")
    doc           <- htmlService.work(id)
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
        relationships = relationships,
        characters = characters.toSet,
        freeformTags = freeformTags.distinct,
        link = Ao3Url.work(id),
        date = {
          doc.stats.updated match
            case Some(date) => PublishedAndUpdated(doc.stats.published, date)
            case None       => Published(doc.stats.published)
        },
        words = doc.stats.words,
        chaptersWritten = doc.stats.chaptersWritten.get,
        chaptersPlanned = doc.stats.chaptersPlanned,
        comments = doc.stats.comments,
        kudos = doc.stats.kudos,
        hits = doc.stats.hits,
        bookmarks = doc.stats.bookmarks
      )
    )
    _       <- ZIO.log(s"Parsed work: $work")
    _       <- log.edit(s"Work parsed, saving to database...")
    flatFic <- data.works.add(work)
  } yield flatFic

  def getSeries(id: String, log: OptionalLog): IO[Lo3Error, FlatFicModel] = for {
    maybeFic <- data.series.getById(id)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None      => parseAndSaveSeries(id, log)
  } yield fic

  private def parseAndSaveSeries(id: String, log: OptionalLog) = for {
    _           <- log.edit(s"Parsing series with id '$id'...")
    pageDocs    <- htmlService.seriesAllPages(id)
    allWorkDocs <- ZIO.succeed(pageDocs.flatMap(_.works))
    newWorkDocs <- ZIO
      .collectAll(allWorkDocs.map(wd => data.works.exists(wd.id).map(exists => if (exists) None else Some(wd))))
      .map(_.flatten)
    _               <- log.edit(s"Parsing fandoms...")
    allFandoms      <- tagService.canonize(newWorkDocs.flatMap(_.fandoms))(tagService.fandom)
    _               <- log.edit(s"Parsing freeform tags...")
    allFreeformTags <- tagService.canonize(newWorkDocs.flatMap(_.freeformTags))(tagService.freeformTag)
    _               <- log.edit(s"Parsing characters...")
    allCharacters   <- tagService.canonize(newWorkDocs.flatMap(_.characters))(tagService.character)
    _               <- log.edit(s"Parsing relationships...")
    allShips <- tagService.canonize(newWorkDocs.flatMap(_.relationships))(tagService.relationship(_: String, allCharacters))
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
            relationships = doc.relationships.map(allShips(_)).toList,
            characters = doc.characters.map(allCharacters(_)).toSet,
            freeformTags = doc.freeformTags.map(allFreeformTags(_)).toList,
            link = Ao3Url.work(id),
            date = SingleDate(doc.stats.date),
            words = doc.stats.words,
            chaptersWritten = doc.stats.chaptersWritten.get,
            chaptersPlanned = doc.stats.chaptersPlanned,
            comments = doc.stats.comments,
            kudos = doc.stats.kudos,
            hits = doc.stats.hits,
            bookmarks = doc.stats.bookmarks
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
    _       <- ZIO.log(s"Parsed series: $series")
    _       <- log.edit(s"Series parsed, saving to database...")
    flatFic <- data.series.add(series)
  } yield flatFic

  private def getWarnings(warnings: Seq[String]): Set[ArchiveWarning] = warnings match {
    case Seq("No Archive Warnings Apply") => Set()
    case otherWarnings                    => otherWarnings.map(ArchiveWarning(_)).toSet
  }

object FicService:
  private val ownLayer = ZLayer {
    ZIO.service[Ao3HttpClient].map(FicServiceImpl(_))
  }

  def live(login: String, password: String): ZLayer[Any, Throwable, FicService] =
    ZLayer.make[FicService](ownLayer, Ao3HttpClientImpl.layer(login, password))
