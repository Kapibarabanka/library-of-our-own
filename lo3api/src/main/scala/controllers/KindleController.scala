package kapibarabanka.lo3.api
package controllers

import ficService.FicService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.models.ao3
import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.{EmailError, KindleEmailNotSet, Lo3Error, UnspecifiedError}
import kapibarabanka.lo3.common.openapi.KindleClient
import kapibarabanka.lo3.common.services.*
import zio.http.*
import zio.process.Command
import zio.{IO, ZIO}

import java.io.File
import java.net.URL
import scala.language.postfixOps
import scala.sys.process.*

case class KindleController(ficService: FicService, bot: MyBotApi) extends Controller:
  private def fileNames(id: String, ficType: FicType) =
    val fileName = AppConfig.ficsPath + ficType.toString + id
    (fileName + ".mobi", fileName + ".epub")

  val sendToKindle = KindleClient.sendToKindle.implement { (key, needToLog) =>
    for {
      log        <- if (needToLog) LogMessage.create("Working on it...", bot, key.userId) else ZIO.succeed(EmptyLog())
      ficTitle   <- getTitle(key.ficId, key.ficType)
      fileName   <- saveFic(key.ficId, key.ficType, ficTitle, log)
      _          <- log.edit("Sending to Kindle...")
      maybeEmail <- Lo3Data.users.getKindleEmail(key.userId)
      email <- maybeEmail match
        case Some(value) => ZIO.succeed(value)
        case None        => ZIO.fail(KindleEmailNotSet())
      _ <- ZIO.attempt(MailClient.sendFile(File(fileName), ficTitle + ".epub", email)).mapError(e => EmailError(e.getMessage))
      _ <- Lo3Data.details.setOnKindle(key, true)
      _ <- log.delete
    } yield ()
  }

  val saveToFile = KindleClient.saveToFile.implement { (ficId, ficType) =>
    for {
      ficTypeMapped <- ZIO.succeed(FicType.valueOf(ficType.toLowerCase.capitalize))
      ficTitle      <- getTitle(ficId, ficTypeMapped)
      result        <- saveFic(ficId, ficTypeMapped, ficTitle, EmptyLog())
    } yield result
  }

  private def saveFic(id: String, ficType: FicType, ficTitle: String, log: OptionalLog): IO[Lo3Error, String] =
    val (_, epubFileName) = fileNames(id, ficType)
    val action =
      if (!File(epubFileName).exists())
        ficType match
          case FicType.Work   => saveWork(id, log)
          case FicType.Series => saveSeries(id, log, ficTitle)
      else
        ZIO.unit
    action.map(_ => epubFileName).mapError(e => UnspecifiedError(e.getMessage))

  private def saveWork(id: String, log: OptionalLog): IO[Throwable, Unit] =
    val (mobi, epub) = fileNames(id, FicType.Work)
    for {
      link    <- ficService.downloadLink(id)
      _       <- log.edit("Downloading work...")
      _       <- ZIO.attempt(new URL(link) #> File(mobi) !!)
      _       <- log.edit("Converting work to epub...")
      process <- Command("ebook-convert", mobi, epub).run
      output  <- process.stdout.string
      _       <- ZIO.attempt(File(mobi).delete())
      _       <- process.successfulExitCode.mapError(_ => Exception(output))
    } yield ()

  private def saveSeries(id: String, log: OptionalLog, title: String) =
    val (_, mergedEpub) = fileNames(id + "merged", FicType.Series)
    val (_, epub)       = fileNames(id, FicType.Series)
    for {
      _            <- log.edit("Getting series' works list...")
      seriesExists <- Lo3Data.series.exists(id)
      workIds      <- if (seriesExists) Lo3Data.series.workIds(id) else ficService.seriesWorks(id)
      _            <- log.edit("Downloading works...")
      workFiles    <- ZIO.collectAll(workIds.map(id => saveFic(id, FicType.Work, epub, log)))
      _            <- log.edit("Merging works in a single file...")
      command <- ZIO.succeed(
        Command("calibre-debug", List("--run-plugin", "EpubMerge", "--", "-t", title, "-o", mergedEpub) ++ workFiles: _*)
      )
      mergeProcess <- command.run
      output       <- mergeProcess.stdout.string
      _            <- mergeProcess.successfulExitCode.mapError(_ => Exception(output))
      _            <- log.edit("Generating cover for merged file...")
      coverProcess <- Command("ebook-convert", mergedEpub, epub).run
      output       <- coverProcess.stdout.string
      _            <- coverProcess.successfulExitCode.mapError(_ => Exception(output))
      _            <- ZIO.attempt(File(mergedEpub).delete())
    } yield ()

  private def getTitle(ficId: String, ficType: FicType) = for {
    maybeTitle <- ficType match
      case FicType.Work   => Lo3Data.works.title(ficId)
      case FicType.Series => Lo3Data.series.title(ficId)
    title <- maybeTitle match
      case Some(value) => ZIO.succeed(value)
      case None        => ficService.ficName(ficId, ficType)
  } yield title

  override val routes: List[Route[Any, Response]] = List(saveToFile, sendToKindle)
