package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError
import com.kapibarabanka.kapibarabot.domain.MyFicRecord
import com.kapibarabanka.kapibarabot.main.Constants.{tempDir, tgFileUrl}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, MailClient, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, Document, InputPartFile, Message}
import zio.*

import java.io.File
import java.net.URL
import scala.language.postfixOps
import scala.sys.process.*

case class SendToKindleScenario(record: MyFicRecord)(implicit
    bot: BotApiWrapper,
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario,
      WithErrorHandling(bot):
  private val sourceFormat = ".mobi"
  private val targetFormat = ".epub"
  protected override def startupAction: UIO[Unit] = (for {
    logLink <- bot.sendText("Getting link from AO3...")
    url     <- ao3.getDownloadLink(record.fic.id)
    logFile <- bot.editLogText(logLink, s"Uploading $sourceFormat file...")
    _       <- useTempFile(url, sourceFormat)(sendFileFromBot)
    _       <- bot.editLogText(logFile, s"Send file below to @ebook_converter_bot and send me the converted $targetFormat file:")
  } yield ()) |> sendOnErrors({})({
    case ao3Error: Ao3ClientError => s"downloading link from AO3"
    case fileError                => s"getting or uploading file"
  })

  override def onMessage(msg: Message): UIO[Scenario] = msg.document match
    case None           => bot.sendText("Not a valid file").flatMap(_ => ExistingFicScenario(record).withStartup)
    case Some(document) => sendToKindle(document)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = StartScenario().onCallbackQuery(query)

  private def sendToKindle(document: Document) = (for {
    logLink    <- bot.sendText("Getting file link from TG...")
    tgFile     <- bot.getFile(document.fileId)
    url        <- ZIO.succeed(tgFileUrl(tgFile.flatMap(_.filePath).getOrElse("FILE_PATH_NOT_FOUND")))
    logSending <- bot.editLogText(logLink, "Downloading file and sending file to Kindle...")
    _          <- useTempFile(url, targetFormat)(sendFileToEmail)
    _ <- bot.editLogText(
      logSending,
      "Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>"
    )
    patchedRecord <- airtable.patchFicStats(
      record.id.get,
      record.stats.copy(isOnKindle = true, kindleToDo = false)
    )
    nextScenario <- ExistingFicScenario(patchedRecord).withStartup
  } yield nextScenario) |> sendOnError("patching record")

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
      _ <- ZIO.attempt(new URL(url) #> temp !!) |> sendOnError({})(s"downloading file from $url")
      _ <- ZIO.log(s"Finished download form $url")
      _ <- action(temp)
    } yield ()

    ZIO.acquireReleaseWith(acquire)(deleteFile)(use) |> sendOnError({})("working with temp file")
  }

  private def sendFileFromBot(file: File) =
    bot.sendDocument(InputPartFile(file)).unit |> sendOnError({})(s"uploading file ${file.getName}")

  private def sendFileToEmail(file: File) =
    ZIO.attempt(MailClient.sendFile(file, record.fic.title + targetFormat)) |> sendOnError({})(
      s"sending file ${file.getName} to kindle email"
    )