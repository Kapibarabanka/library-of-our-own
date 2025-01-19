package kapibarabanka.lo3.api
package ficService.internal

import kapibarabanka.lo3.common.models.ao3.*
import kapibarabanka.lo3.common.models.ao3.RelationshipType.*
import kapibarabanka.lo3.common.models.domain.Lo3Error
import zio.*

class TagService(html: HtmlService):
  def character(nameInWork: String): IO[Lo3Error, Character] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Character.fromNameInWork(canonicalName)

  def relationship(nameInWork: String, characters: Map[String, Character] = Map()): IO[Lo3Error, Relationship] = {
    def characterWithLabel(name: String, label: Option[String]): IO[Lo3Error, Character] =
      val nameWithLabel = Ao3TagName.combineWithLabel(name, label)
      if (characters.contains(nameWithLabel))
        ZIO.succeed(characters(nameWithLabel))
      if (characters.contains(name))
        ZIO.succeed(characters(name))
      html
        .tagExists(nameWithLabel)
        .map(exists => if (exists) nameWithLabel else name)
        .flatMap(resultName => character(resultName))

    val isRomantic = nameInWork.contains("/")
    val separator  = if (isRomantic) "/" else " & "
    val shipType   = if (isRomantic) Romantic else Platonic
    for {
      canonicalShipName <- getCanonicalTagName(nameInWork)
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

  def fandom(nameInWork: String): IO[Lo3Error, Fandom] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Fandom.fromNameInWork(canonicalName)

  def freeformTag(nameInWork: String): IO[Lo3Error, FreeformTag] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield FreeformTag(canonicalName)

  def canonize[TTag](tagNames: Seq[String])(
      canonize: String => IO[Lo3Error, TTag]
  ): IO[Lo3Error, Map[String, TTag]] =
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

  private def getCanonicalTagName(tagName: String): IO[Lo3Error, String] = for {
    _              <- ZIO.log(s"Getting canonical name for tag '$tagName'")
    maybeCanonical <- data.tags.tryGetCanonical(tagName)
    canonicalName <- maybeCanonical match
      case Some(name) => ZIO.succeed(name)
      case None =>
        for {
          doc       <- html.tag(tagName)
          canonical <- ZIO.succeed(doc.canonicalName.getOrElse(tagName))
          _         <- ZIO.log(s"Canonical name for'$tagName' is '${canonical}'")
          _         <- data.tags.addCanonical(tagName, canonical, doc.isFilterable)
        } yield canonical
  } yield canonicalName
