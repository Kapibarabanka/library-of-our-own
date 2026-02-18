package kapibarabanka.lo3.api
package services.ao3Info.internal

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3.*
import kapibarabanka.lo3.common.models.ao3.RelationshipType.*
import kapibarabanka.lo3.common.models.domain.Lo3Error
import zio.*

class TagService(html: HtmlService):
  def character(nameInWork: String): IO[Lo3Error, Character] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Character.fromNameInWork(canonicalName)

  def relationship(nameInWork: String): IO[Lo3Error, Relationship | String] = {
    val isRomantic = nameInWork.contains("/")
    val separator  = if (isRomantic) "/" else " & "
    val shipType   = if (isRomantic) Romantic else Platonic
    for {
      canonicalShipName <- getCanonicalTagName(nameInWork)
      shipExists        <- Lo3Data.tags.shipExists(canonicalShipName)
      result <-
        if (shipExists) ZIO.succeed(canonicalShipName)
        else
          for {
            (ship, label)  <- ZIO.succeed(Ao3TagName.trySeparateLabel(canonicalShipName))
            characterNames <- ZIO.succeed(ship.split(separator))
            characters     <- ZIO.collectAll(characterNames.toSet.map(name => characterWithLabel(name, label)))
          } yield Relationship(
            characters,
            shipType,
            // e.g Alphonse Elric/Cats is a synonym of Alphonse Elric/Other(s) and it's not very informative
            if (characters.contains(Character("Other(s)", None))) Some(nameInWork) else None
          )
    } yield result
  }

  def fandom(nameInWork: String): IO[Lo3Error, Fandom] = for {
    canonicalName <- getCanonicalTagName(nameInWork)
  } yield Fandom.fromNameInWork(canonicalName)

  def freeformTag(nameInWork: String): IO[Lo3Error, FreeformTag] = for {
    _ <- getCanonicalTagName(nameInWork)
  } yield FreeformTag(nameInWork)

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
    maybeCanonical <- Lo3Data.tags.tryGetCanonical(tagName)
    canonicalName <- maybeCanonical match
      case Some(name) => ZIO.succeed(name)
      case None =>
        for {
          doc       <- html.tag(tagName)
          canonical <- ZIO.succeed(doc.canonicalName.getOrElse(tagName))
          _         <- ZIO.log(s"Canonical name for'$tagName' is '${canonical}'")
          _         <- Lo3Data.tags.addCanonical(tagName, canonical, doc.isFilterable)
        } yield canonical
  } yield canonicalName

  private def characterWithLabel(name: String, label: Option[String]): IO[Lo3Error, Character] =
    val nameWithLabel = Ao3TagName.combineWithLabel(name, label)
    for {
      maybeCanonical <- Lo3Data.tags.tryGetCanonical(nameWithLabel)
      maybeCanonical <- if (maybeCanonical.isEmpty) Lo3Data.tags.tryGetCanonical(name) else ZIO.succeed(maybeCanonical)
      canonicalName <- maybeCanonical match
        case Some(name) => ZIO.succeed(name)
        case None =>
          for {
            canonicalName <- html
              .tagExists(nameWithLabel)
              .map(exists => if (exists) nameWithLabel else name)
            _ <- ZIO.log(s"Canonical name for'$name' is '${canonicalName}'")
            _ <- Lo3Data.tags.addCanonical(name, canonicalName, false)
          } yield canonicalName
      character <- character(canonicalName)
    } yield character
