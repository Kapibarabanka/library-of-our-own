package com.kapibarabanka.ao3scrapper

import com.kapibarabanka.ao3scrapper.docs.*
import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError
import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError.*
import com.kapibarabanka.ao3scrapper.models.*
import com.kapibarabanka.ao3scrapper.models.RelationshipType.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import zio.{IO, ZIO, ZLayer}

trait Ao3:
  def work(id: String): IO[Ao3ClientError, Work]
  def series(id: String): IO[Ao3ClientError, Series]
  def character(nameInWork: String): IO[Ao3ClientError, Character]
  def relationship(nameInWork: String): IO[Ao3ClientError, Relationship]
  def fandom(nameInWork: String): IO[Ao3ClientError, Fandom]
  def freeformTag(nameInWork: String): IO[Ao3ClientError, FreeformTag]
  def getCanonicalTagName(tagName: String): IO[Ao3ClientError, Option[String]]
  def getDownloadLink(workId: String): IO[Ao3ClientError, String]

case class Ao3Impl(http: Ao3HttpClient) extends Ao3:
  // TODO: create config for these
  private val canonizeFreeformTags       = true
  private val canonizeShipsAndCharacters = true
  private val canonizeFandoms            = true
  private val getTagsFromClient          = true

  private val fandomPattern = """^(.*)\s(\(.*\))$""".r
  private val jsoupBrowser  = JsoupBrowser()

  override def work(id: String): IO[Ao3ClientError, Work] = for {
    _             <- ZIO.log(s"Parsing work with id '$id'")
    doc           <- getWorkDoc(id)
    _             <- ZIO.log(s"Parsing fandoms")
    fandoms       <- ZIO.collectAllPar(doc.fandoms.map(fandom))
    _             <- ZIO.log(s"Parsing characters")
    characters    <- ZIO.collectAllPar(doc.characters.map(character))
    _             <- ZIO.log(s"Parsing relationships")
    relationships <- ZIO.collectAllPar(doc.relationships.map(relationship))
    _             <- ZIO.log(s"Parsing freeform tags")
    freeformTags  <- ZIO.collectAllPar(doc.freeformTags.map(freeformTag))
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
        started = doc.stats.published,
        updated = doc.stats.updated,
        words = doc.stats.words,
        partsWritten = doc.stats.chaptersWritten.get,
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
    _             <- ZIO.log(s"Parsing series with id '$id'")
    doc           <- getSeriesDoc(id)
    _             <- ZIO.log(s"Parsing fandoms")
    fandoms       <- ZIO.collectAll(doc.fandoms.map(fandom))
    _             <- ZIO.log(s"Parsing characters")
    characters    <- ZIO.collectAll(doc.characters.map(character))
    _             <- ZIO.log(s"Parsing relationships")
    relationships <- ZIO.collectAll(doc.relationships.map(relationship))
    _             <- ZIO.log(s"Parsing freeform tags")
    freeformTags  <- ZIO.collectAll(doc.freeformTags.map(freeformTag))
    series <- ZIO.succeed(
      Series(
        id = id,
        title = doc.title,
        authors = doc.authors.toList,
        rating = doc.ratings.map(Rating.withName).maxBy(_.id),
        warnings = doc.allWarnings.flatMap(w => getWarnings(w.split(", "))).toSet,
        categories = {
          doc.allCategories.flatMap(c => c.split(", ").map(Category.withName)).distinct match
            case List(Category.None) => Set()
            case xs                  => xs.toSet
        },
        fandoms = fandoms.toSet,
        relationships = relationships,
        characters = characters,
        freeformTags = freeformTags,
        link = Ao3Url.work(id),
        started = doc.begun.get,
        updated = doc.updated,
        words = doc.words.get,
        complete = doc.complete.get,
        bookmarks = doc.bookmarks,
        workIds = doc.workIds,
        description = doc.description
      )
    )
    _ <- ZIO.log(s"Parsed series: $series")
  } yield series

  override def character(nameInWork: String): IO[Ao3ClientError, Character] =
    if (!canonizeShipsAndCharacters)
      ZIO.succeed(Character(nameInWork))
    else
      for {
        canonicalName <- getCanonicalTagName(nameInWork)
      } yield Character(canonicalName.getOrElse(nameInWork))

  override def relationship(nameInWork: String): IO[Ao3ClientError, Relationship] = {
    val isRomantic = nameInWork.contains("/")
    val separator  = if (isRomantic) "/" else " & "
    val shipType   = if (isRomantic) Romantic else Platonic
    if (!canonizeShipsAndCharacters)
      ZIO.succeed(Relationship(nameInWork.split(separator).map(Character(_)).toSet, shipType, None))
    else
      for {
        name <- getCanonicalTagName(nameInWork).map(n => n.getOrElse(nameInWork))
        characterNames <- ZIO.succeed(name match
          // todo if tag "character (fandom)" doesnt exist try to canonize just "character"
          case fandomPattern(shipWithoutFandom, fandom) => shipWithoutFandom.split(separator).map(c => s"$c $fandom")
          case shipWithoutFandom                        => shipWithoutFandom.split(separator)
        )
        characters <- ZIO.collectAll(characterNames.map(character))
      } yield Relationship(
        characters.toSet,
        shipType,
        // e.g Alphonse Elric/Cats is a synonym of Alphonse Elric/Other(s) and it's not very informative
        if (characters.contains(Character("Other(s)"))) Some(nameInWork) else None
      )
  }

  override def fandom(nameInWork: String): IO[Ao3ClientError, Fandom] =
    if (!canonizeFandoms)
      ZIO.succeed(Fandom(nameInWork))
    else
      for {
        canonicalName <- getCanonicalTagName(nameInWork)
      } yield Fandom(canonicalName.getOrElse(nameInWork))

  override def freeformTag(nameInWork: String): IO[Ao3ClientError, FreeformTag] =
    if (!canonizeFreeformTags)
      ZIO.succeed(FreeformTag(nameInWork, None))
    else
      for {
        doc <- getTagDoc(nameInWork)
      } yield FreeformTag(doc.canonicalName.getOrElse(nameInWork), Some(doc.isFilterable))

  override def getCanonicalTagName(tagName: String): IO[Ao3ClientError, Option[String]] = for {
    _   <- ZIO.log(s"Getting canonical name for tag '$tagName'")
    doc <- getTagDoc(tagName)
  } yield doc.canonicalName

  // TODO: add more formats
  override def getDownloadLink(workId: String): IO[Ao3ClientError, String] = for {
    doc  <- getWorkDoc(workId)
    link <- doc.mobiLink.fold[IO[Ao3ClientError, String]](ZIO.fail(LinkNotFound(workId)))(s => ZIO.succeed(s))
  } yield Ao3Url.base + link

  private def getWorkDoc(id: String)   = http.getAuthed(Ao3Url.work(id)).map(html => WorkDoc(jsoupBrowser.parseString(html)))
  private def getSeriesDoc(id: String) = http.getAuthed(Ao3Url.series(id)).map(html => SeriesDoc(jsoupBrowser.parseString(html)))
  private def getTagDocFromClient(id: String) = http.get(Ao3Url.tag(id)).map(html => TagDoc(jsoupBrowser.parseString(html)))
  private def getTagDocFromJsoup(id: String)  = ZIO.succeed(TagDoc(jsoupBrowser.get(Ao3Url.tag(id))))

  private val getTagDoc = if (getTagsFromClient) getTagDocFromClient else getTagDocFromJsoup

  private def getWarnings(warnings: Seq[String]): Set[ArchiveWarning] = warnings match {
    case Seq("No Archive Warnings Apply") => Set()
    case otherWarnings                    => otherWarnings.map(ArchiveWarning(_)).toSet
  }

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

  def relationship(nameInWork: String): ZIO[Ao3, Ao3ClientError, Relationship] =
    ZIO.serviceWithZIO[Ao3](_.relationship(nameInWork))

  def fandom(nameInWork: String): ZIO[Ao3, Ao3ClientError, Fandom] =
    ZIO.serviceWithZIO[Ao3](_.fandom(nameInWork))

  def freeformTag(nameInWork: String): ZIO[Ao3, Ao3ClientError, FreeformTag] =
    ZIO.serviceWithZIO[Ao3](_.freeformTag(nameInWork))

  def getCanonicalTagName(tagName: String): ZIO[Ao3, Ao3ClientError, Option[String]] =
    ZIO.serviceWithZIO[Ao3](_.getCanonicalTagName(tagName))

  def getDownloadLink(workId: String): ZIO[Ao3, Ao3ClientError, String] =
    ZIO.serviceWithZIO[Ao3](_.getDownloadLink(workId))
