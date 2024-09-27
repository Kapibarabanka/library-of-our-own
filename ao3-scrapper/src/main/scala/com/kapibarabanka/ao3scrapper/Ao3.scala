package com.kapibarabanka.ao3scrapper

import com.kapibarabanka.ao3scrapper.Ao3Error.*
import com.kapibarabanka.ao3scrapper.domain.*
import com.kapibarabanka.ao3scrapper.domain.RelationshipType.*
import com.kapibarabanka.ao3scrapper.internal.docs.{SeriesPageDoc, TagDoc, WorkDoc}
import com.kapibarabanka.ao3scrapper.internal.{Ao3HttpClient, Ao3HttpClientImpl}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.jsoup.HttpStatusException
import zio.{IO, ZIO, ZLayer}

import scala.util.{Failure, Success, Try}

trait Ao3:
  def work(id: String): IO[Ao3Error, Work]
  def series(id: String): IO[Ao3Error, Series]
  def character(nameInWork: String): IO[Ao3Error, Character]
  def relationship(nameInWork: String, characters: Map[String, Character] = Map()): IO[Ao3Error, Relationship]
  def fandom(nameInWork: String): IO[Ao3Error, Fandom]
  def freeformTag(nameInWork: String): IO[Ao3Error, FreeformTag]
  def getCanonicalTagName(tagName: String): IO[Ao3Error, Option[String]]
  def getDownloadLink(workId: String): IO[Ao3Error, String]

case class Ao3Impl(http: Ao3HttpClient) extends Ao3:
  private val getTagsFromClient = true

  private val jsoupBrowser = JsoupBrowser()

  override def work(id: String): IO[Ao3Error, Work] = for {
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

  override def series(id: String): IO[Ao3Error, Series] = for {
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

  override def character(nameInWork: String): IO[Ao3Error, Character] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Character.fromNameInWork(canonicalName.getOrElse(nameInWork))

  override def relationship(nameInWork: String, characters: Map[String, Character] = Map()): IO[Ao3Error, Relationship] = {
    def characterWithLabel(name: String, label: Option[String]): IO[Ao3Error, Character] =
      val nameWithLabel = Ao3TagName.combineWithLabel(name, label)
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
      (ship, label)     <- ZIO.succeed(Ao3TagName.trySeparateLabel(canonicalShipName))
      characterNames    <- ZIO.succeed(ship.split(separator))
      characters        <- ZIO.collectAll(characterNames.toSet.map(name => characterWithLabel(name, label)))
    } yield Relationship(
      characters,
      shipType,
      // e.g Alphonse Elric/Cats is a synonym of Alphonse Elric/Other(s) and it's not very informative
      if (characters.contains(Character("Other(s)", None))) Some(nameInWork) else None
    )
  }

  override def fandom(nameInWork: String): IO[Ao3Error, Fandom] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Fandom.fromNameInWork(canonicalName.getOrElse(nameInWork))

  override def freeformTag(nameInWork: String): IO[Ao3Error, FreeformTag] = for {
    _   <- ZIO.log(s"Getting canonical name for tag '$nameInWork'")
    doc <- getTagDoc(nameInWork)
    _   <- ZIO.log(s"Canonical name for'$nameInWork' is '${doc.canonicalName.getOrElse(nameInWork)}'")
  } yield FreeformTag(doc.canonicalName.getOrElse(nameInWork), Some(doc.isFilterable))

  override def getCanonicalTagName(tagName: String): IO[Ao3Error, Option[String]] = for {
    _   <- ZIO.log(s"Getting canonical name for tag '$tagName'")
    doc <- getTagDoc(tagName)
    _   <- ZIO.log(s"Canonical name for'$tagName' is '${doc.canonicalName.getOrElse(tagName)}'")
  } yield doc.canonicalName

  // TODO: add more formats
  override def getDownloadLink(workId: String): IO[Ao3Error, String] = for {
    doc  <- getWorkDoc(workId)
    link <- doc.mobiLink.fold[IO[Ao3Error, String]](ZIO.fail(DownloadLinkNotFound(workId)))(s => ZIO.succeed(s))
  } yield Ao3Url.download(link)

  private def canonize[TTag](tagNames: Seq[String])(
      canonize: String => IO[Ao3Error, TTag]
  ): IO[Ao3Error, Map[String, TTag]] =
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

  private def getWorkDoc(id: String) =
    getDoc(Ao3Url.work(id), true, s"work with id $id")(html => WorkDoc(html))

  private def getSeriesFirstPage(id: String) =
    getDoc(Ao3Url.series(id), true, s"series with id $id")(html => SeriesPageDoc(html, id, 1))

  private def getSeriesPageDocs(id: String) = for {
    entityName <- ZIO.succeed(s"series with id $id")
    firstPage  <- getSeriesFirstPage(id)
    allPages <- firstPage.pageCount match
      case None => ZIO.succeed(List(firstPage))
      case Some(pageCount) =>
        ZIO.collectAll(
          for i <- 1 to pageCount
          yield getDoc(Ao3Url.seriesPage(id, i), true, entityName)(html => SeriesPageDoc(html, id, i))
        )
  } yield allPages.toList

  private def getTagDocFromClient(id: String) = getDoc(Ao3Url.tag(id), false, s"tag '$id'")(html => TagDoc(html))
  private def getTagDocFromJsoup(id: String) = for {
    entityName <- ZIO.succeed(s"tag '$id'")
    html       <- ZIO.attempt(jsoupBrowser.get(Ao3Url.tag(id))).mapError(e => UnspecifiedError(e.toString))
    doc        <- tryParseToDoc(html, entityName)(html => TagDoc(html))
  } yield doc

  private val getTagDoc = if (getTagsFromClient) getTagDocFromClient else getTagDocFromJsoup

  private def getWarnings(warnings: Seq[String]): Set[ArchiveWarning] = warnings match {
    case Seq("No Archive Warnings Apply") => Set()
    case otherWarnings                    => otherWarnings.map(ArchiveWarning(_)).toSet
  }

  private def tagExists(tag: String) = Try(jsoupBrowser.get(Ao3Url.tag(tag))) match
    case Failure(exception: HttpStatusException) =>
      if (exception.getStatusCode == 404) ZIO.succeed(false)
      else ZIO.fail(HttpError(exception.getStatusCode, s"checking if tag '$tag' exists"))
    case Failure(exception) => ZIO.fail(UnspecifiedError(exception.getMessage))
    case Success(_)         => ZIO.succeed(true)

  private def getDoc[TDoc](url: String, authed: Boolean, entityName: String)(htmlToDoc: Document => TDoc) = for {
    body <- (if (authed) http.getAuthed(url) else http.get(url)).mapError {
      case NotFound(_) => NotFound(entityName)
      case e           => e
    }
    html <- ZIO.attempt(jsoupBrowser.parseString(body)).mapError(e => ParsingError(e.toString, entityName))
    doc  <- tryParseToDoc(html, entityName)(htmlToDoc)
  } yield doc

  private def tryParseToDoc[TDoc](html: Document, entityName: String)(parse: Document => TDoc) = {
    ZIO.attempt(parse(html)).mapError {
      case noSuchElement: NoSuchElementException =>
        ParsingError("Cannot parse html into custom doc: element not found exception", entityName)
      case e => ParsingError(e.toString, entityName)
    }
  }

object Ao3:
  private val ownLayer = ZLayer {
    ZIO.service[Ao3HttpClient].map(Ao3Impl(_))
  }

  def live(login: String, password: String): ZLayer[Any, Throwable, Ao3] =
    ZLayer.make[Ao3](ownLayer, Ao3HttpClientImpl.layer(login, password))

  def work(id: String): ZIO[Ao3, Ao3Error, Work] =
    ZIO.serviceWithZIO[Ao3](_.work(id))

  def series(id: String): ZIO[Ao3, Ao3Error, Series] =
    ZIO.serviceWithZIO[Ao3](_.series(id))

  def character(nameInWork: String): ZIO[Ao3, Ao3Error, Character] =
    ZIO.serviceWithZIO[Ao3](_.character(nameInWork))

  def relationship(nameInWork: String, characters: Map[String, Character] = Map()): ZIO[Ao3, Ao3Error, Relationship] =
    ZIO.serviceWithZIO[Ao3](_.relationship(nameInWork, characters))

  def fandom(nameInWork: String): ZIO[Ao3, Ao3Error, Fandom] =
    ZIO.serviceWithZIO[Ao3](_.fandom(nameInWork))

  def freeformTag(nameInWork: String): ZIO[Ao3, Ao3Error, FreeformTag] =
    ZIO.serviceWithZIO[Ao3](_.freeformTag(nameInWork))

  def getCanonicalTagName(tagName: String): ZIO[Ao3, Ao3Error, Option[String]] =
    ZIO.serviceWithZIO[Ao3](_.getCanonicalTagName(tagName))

  def getDownloadLink(workId: String): ZIO[Ao3, Ao3Error, String] =
    ZIO.serviceWithZIO[Ao3](_.getDownloadLink(workId))
