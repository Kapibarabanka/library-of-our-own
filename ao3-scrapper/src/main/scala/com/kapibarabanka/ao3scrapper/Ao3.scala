package com.kapibarabanka.ao3scrapper

import com.kapibarabanka.ao3scrapper.docs.*
import Ao3ClientError.*
import com.kapibarabanka.ao3scrapper.models.*
import com.kapibarabanka.ao3scrapper.models.RelationshipType.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.HttpStatusException
import zio.{IO, ZIO, ZLayer}

import scala.util.{Failure, Success, Try}

trait Ao3:
  def work(id: String): IO[Ao3ClientError, Work]
  def series(id: String): IO[Ao3ClientError, Series]
  def character(nameInWork: String): IO[Ao3ClientError, Character]
  def relationship(nameInWork: String, characters: Map[String, Character] = Map()): IO[Ao3ClientError, Relationship]
  def fandom(nameInWork: String): IO[Ao3ClientError, Fandom]
  def freeformTag(nameInWork: String): IO[Ao3ClientError, FreeformTag]
  def getCanonicalTagName(tagName: String): IO[Ao3ClientError, Option[String]]
  def getDownloadLink(workId: String): IO[Ao3ClientError, String]

case class Ao3Impl(http: Ao3HttpClient) extends Ao3:
  private val getTagsFromClient = true

  private val jsoupBrowser = JsoupBrowser()

  override def work(id: String): IO[Ao3ClientError, Work] = for {
    _             <- ZIO.log(s"Parsing work with id '$id'")
    doc           <- getWorkDoc(id)
    _             <- ZIO.log(s"Parsing fandoms")
    fandoms       <- ZIO.collectAll(doc.fandoms.map(fandom))
    _             <- ZIO.log(s"Parsing characters")
    characters    <- ZIO.collectAll(doc.characters.map(character))
    _             <- ZIO.log(s"Parsing relationships")
    relationships <- ZIO.collectAll(doc.relationships.map(relationship(_)))
    _             <- ZIO.log(s"Parsing freeform tags")
    freeformTags  <- ZIO.collectAll(doc.freeformTags.map(freeformTag))
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
        freeformTags = freeformTags,
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
    _ <- ZIO.log(s"Parsed work: $work")
  } yield work

  override def series(id: String): IO[Ao3ClientError, Series] = for {
    _               <- ZIO.log(s"Parsing series with id '$id'")
    pageDocs        <- getSeriesPageDocs(id)
    firstPage       <- ZIO.succeed(pageDocs.head)
    workDocs        <- ZIO.succeed(pageDocs.flatMap(_.works))
    _               <- ZIO.log(s"Parsing fandoms")
    allFandoms      <- canonize(workDocs.flatMap(_.fandoms))(fandom)
    _               <- ZIO.log(s"Parsing freeform tags")
    allFreeformTags <- canonize(workDocs.flatMap(_.freeformTags))(freeformTag)
    _               <- ZIO.log(s"Parsing characters")
    allCharacters   <- canonize(workDocs.flatMap(_.characters))(character)
    _               <- ZIO.log(s"Parsing relationships")
    allShips        <- canonize(workDocs.flatMap(_.relationships))(relationship(_: String, allCharacters))
    works <- ZIO.succeed(
      workDocs
        .map(doc =>
          (
            doc.indexInSeries,
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
        .sortBy(_._1)
        .map(_._2)
    )
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
        works = works
      )
    )
    _ <- ZIO.log(s"Parsed series: $series")
  } yield series

  override def character(nameInWork: String): IO[Ao3ClientError, Character] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Character.fromNameInWork(canonicalName.getOrElse(nameInWork))

  override def relationship(nameInWork: String, characters: Map[String, Character] = Map()): IO[Ao3ClientError, Relationship] = {
    def characterWithLabel(name: String, label: Option[String]): IO[Ao3ClientError, Character] =
      val nameWithLabel = StringUtils.combineWithLabel(name, label)
      if (characters.contains(nameWithLabel))
        ZIO.succeed(characters(nameWithLabel))
      if (characters.contains(name))
        ZIO.succeed(characters(name))
      tagExists(nameWithLabel)
        .map(exists => if (exists) nameWithLabel else name)
        .flatMap(resultName => character(resultName))

    val isRomantic = nameInWork.contains("/")
    val separator  = if (isRomantic) "/" else " & "
    val shipType   = if (isRomantic) Romantic else Platonic
    for {
      canonicalShipName <- getCanonicalTagName(nameInWork).map(n => n.getOrElse(nameInWork))
      (ship, label)     <- ZIO.succeed(StringUtils.trySeparateLabel(canonicalShipName))
      characterNames    <- ZIO.succeed(ship.split(separator))
      characters        <- ZIO.collectAll(characterNames.toSet.map(name => characterWithLabel(name, label)))
    } yield Relationship(
      characters,
      shipType,
      // e.g Alphonse Elric/Cats is a synonym of Alphonse Elric/Other(s) and it's not very informative
      if (characters.contains(Character("Other(s)", None))) Some(nameInWork) else None
    )
  }

  override def fandom(nameInWork: String): IO[Ao3ClientError, Fandom] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Fandom.fromNameInWork(canonicalName.getOrElse(nameInWork))

  override def freeformTag(nameInWork: String): IO[Ao3ClientError, FreeformTag] = for {
    _   <- ZIO.log(s"Getting canonical name for tag '$nameInWork'")
    doc <- getTagDoc(nameInWork)
    _   <- ZIO.log(s"Canonical name for'$nameInWork' is '${doc.canonicalName.getOrElse(nameInWork)}'")
  } yield FreeformTag(doc.canonicalName.getOrElse(nameInWork), Some(doc.isFilterable))

  override def getCanonicalTagName(tagName: String): IO[Ao3ClientError, Option[String]] = for {
    _   <- ZIO.log(s"Getting canonical name for tag '$tagName'")
    doc <- getTagDoc(tagName)
    _   <- ZIO.log(s"Canonical name for'$tagName' is '${doc.canonicalName.getOrElse(tagName)}'")
  } yield doc.canonicalName

  // TODO: add more formats
  override def getDownloadLink(workId: String): IO[Ao3ClientError, String] = for {
    doc  <- getWorkDoc(workId)
    link <- doc.mobiLink.fold[IO[Ao3ClientError, String]](ZIO.fail(LinkNotFound(workId)))(s => ZIO.succeed(s))
  } yield Ao3Url.download(link)

  private def canonize[TTag](tagNames: Seq[String])(
      canonize: String => IO[Ao3ClientError, TTag]
  ): IO[Ao3ClientError, Map[String, TTag]] =
    tagNames.foldLeft(ZIO.succeed(Map[String, TTag]()))((mapZIO, name) =>
      for {
        map <- mapZIO
        newMap <-
          if (map.contains(name)) ZIO.succeed(map)
          else
            for {
              canonized <- canonize(name)
            } yield map + (name -> canonized)
      } yield newMap
    )

  private def getWorkDoc(id: String) = http.getAuthed(Ao3Url.work(id)).map(html => WorkDoc(jsoupBrowser.parseString(html)))

  private def getSeriesFirstPage(id: String) =
    http.getAuthed(Ao3Url.series(id)).map(html => SeriesPageDoc(jsoupBrowser.parseString(html), id, 1))

  private def getSeriesPageDocs(id: String) = for {
    firstPage <- getSeriesFirstPage(id)
    allPages <- firstPage.pageCount match
      case None => ZIO.succeed(List(firstPage))
      case Some(pageCount) =>
        ZIO.collectAll(
          for i <- 1 to pageCount
          yield http.getAuthed(Ao3Url.seriesPage(id, i)).map(html => SeriesPageDoc(jsoupBrowser.parseString(html), id, i))
        )
  } yield allPages.toList

  private def getTagDocFromClient(id: String) = http.get(Ao3Url.tag(id)).map(html => TagDoc(jsoupBrowser.parseString(html)))
  private def getTagDocFromJsoup(id: String)  = ZIO.succeed(TagDoc(jsoupBrowser.get(Ao3Url.tag(id))))

  private val getTagDoc = if (getTagsFromClient) getTagDocFromClient else getTagDocFromJsoup

  private def getWarnings(warnings: Seq[String]): Set[ArchiveWarning] = warnings match {
    case Seq("No Archive Warnings Apply") => Set()
    case otherWarnings                    => otherWarnings.map(ArchiveWarning(_)).toSet
  }

  private def tagExists(tag: String) = Try(jsoupBrowser.get(Ao3Url.tag(tag))) match
    case Failure(exception: HttpStatusException) =>
      if (exception.getStatusCode == 404) ZIO.succeed(false) else ZIO.fail(HttpError(exception.getMessage))
    case Failure(exception) => ZIO.fail(HttpError(exception.getMessage))
    case Success(_)         => ZIO.succeed(true)

object Ao3Impl:
  val layer: ZLayer[Ao3HttpClient, Nothing, Ao3Impl] = ZLayer {
    for {
      http <- ZIO.service[Ao3HttpClient]
    } yield Ao3Impl(http)
  }

object Ao3:
  def work(id: String): ZIO[Ao3, Ao3ClientError, Work] =
    ZIO.serviceWithZIO[Ao3](_.work(id))

  def series(id: String): ZIO[Ao3, Ao3ClientError, Series] =
    ZIO.serviceWithZIO[Ao3](_.series(id))

  def character(nameInWork: String): ZIO[Ao3, Ao3ClientError, Character] =
    ZIO.serviceWithZIO[Ao3](_.character(nameInWork))

  def relationship(nameInWork: String, characters: Map[String, Character] = Map()): ZIO[Ao3, Ao3ClientError, Relationship] =
    ZIO.serviceWithZIO[Ao3](_.relationship(nameInWork, characters))

  def fandom(nameInWork: String): ZIO[Ao3, Ao3ClientError, Fandom] =
    ZIO.serviceWithZIO[Ao3](_.fandom(nameInWork))

  def freeformTag(nameInWork: String): ZIO[Ao3, Ao3ClientError, FreeformTag] =
    ZIO.serviceWithZIO[Ao3](_.freeformTag(nameInWork))

  def getCanonicalTagName(tagName: String): ZIO[Ao3, Ao3ClientError, Option[String]] =
    ZIO.serviceWithZIO[Ao3](_.getCanonicalTagName(tagName))

  def getDownloadLink(workId: String): ZIO[Ao3, Ao3ClientError, String] =
    ZIO.serviceWithZIO[Ao3](_.getDownloadLink(workId))
