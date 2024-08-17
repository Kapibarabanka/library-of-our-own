package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError
import com.kapibarabanka.kapibarabot.MailClient
import com.kapibarabanka.kapibarabot.bot.Constants.{myChatId, tempDir, tgFileUrl}
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.{getFile, sendDocument}
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, Document, InputPartFile, Message}
import zio.*
import scalaz.Scalaz.ToIdOps

import java.io.File
import java.net.URL
import scala.language.postfixOps
import scala.sys.process.*

case class SendToKindleScenario(record: MyFicRecord)(implicit
    bot: Api[Task],
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario:
  private val sourceFormat = ".mobi"
  private val targetFormat = ".epub"
  protected override def startupAction: UIO[Unit] = {
    val upload = for {
      logLink <- sendText("Getting link from AO3...")
      url     <- ao3.getDownloadLink(record.fic.id)
      logFile <- editLogText(logLink, s"Uploading $sourceFormat file...")
      _       <- useTempFile(url, sourceFormat)(sendFileFromBot)
      _       <- editLogText(logFile, s"Send file below to @ebook_converter_bot and send me the converted $targetFormat file:")
    } yield ()
    upload.foldZIO(
      error => sendText(error match
        case ao3Error: Ao3ClientError => s"Couldn't get download link from AO3: ${ao3Error.getMessage}"
        case fileError => s"Error while getting or uploading file: ${fileError.getMessage}" 
      ).map(_ => StartScenario()), 
      scenario => ZIO.succeed(scenario)
    )
  }

  override def onMessage(msg: Message): UIO[Scenario] = msg.document match
    case None => sendText("Not a valid file").flatMap(_ => ExistingFicScenario(record).withStartup)
    case Some(document) => sendToKindle(document) |> tryAndSendOnError()

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = StartScenario().onCallbackQuery(query)
  
  private def sendToKindle(document: Document) = for {
    logLink    <- sendText("Getting file link from TG...")
    tgFile     <- getFile(document.fileId).exec
    url        <- ZIO.succeed(tgFileUrl(tgFile.filePath.getOrElse("FILE_PATH_NOT_FOUND")))
    logSending <- editLogText(logLink, "Downloading file and sending file to Kindle...")
    _          <- useTempFile(url, targetFormat)(sendFileToEmail)
    _ <- editLogText(
      logSending,
      "Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>"
    )
    patchedRecord <- airtable.patchFicStats(
      record.id.get,
      MyFicStats(isOnKindleOption = Some(true), kindleToDoOption = Some(false))
    )
    nextScenario <- ExistingFicScenario(patchedRecord).withStartup
  } yield nextScenario
  
  private def useTempFile(url: String, fileFormat: String)(action: File => Task[Unit]) = {
    def acquire = for {
      file <- ZIO.attempt(File(tempDir + record.fic.title + fileFormat))
      _    <- ZIO.log(s"Created file ${file.getPath}")
    } yield file

    def deleteFile(temp: File) = for {
      path <- ZIO.succeed(temp.getPath)
      _    <- ZIO.succeed(temp.delete())
      _    <- ZIO.log(s"Deleted file $path")
    } yield ()

    def use(temp: File) = for {
      _ <- ZIO.log(s"Downloading file form $url")
      _ <- ZIO.attempt(new URL(url) #> temp !!)
      _ <- ZIO.log(s"Finished download form $url")
      _ <- action(temp)
    } yield ()

    ZIO.acquireReleaseWith(acquire)(deleteFile)(use)
  }

  private def sendFileFromBot(file: File) = sendDocument(myChatId, InputPartFile(file)).exec.unit
  private def sendFileToEmail(file: File) = ZIO.attempt(MailClient.sendFile(file, record.fic.title + targetFormat))
