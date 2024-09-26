package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3ClientError}
import com.kapibarabanka.kapibarabot.services.{BotWithChatId, DbService}
import com.kapibarabanka.kapibarabot.utils
import com.kapibarabanka.kapibarabot.utils.Constants.tgFileUrl
import com.kapibarabanka.kapibarabot.utils.MailClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, Document, InputPartFile, Message}
import zio.*

import java.io.File
import java.net.URL
import scala.language.postfixOps
import scala.sys.process.*

case class SendToKindleStateProcessor(state: SendToKindleBotState, bot: BotWithChatId, db: DbService, ao3: Ao3)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):
  private val sourceFormat = ".mobi"
  private val targetFormat = ".epub"

  override def startup: UIO[Unit] = (for {
    logLink <- bot.sendText("Getting link from AO3...")
    url     <- ao3.getDownloadLink(state.ficToSend.fic.id)
    logFile <- bot.editLogText(logLink, s"Uploading $sourceFormat file...")
    _       <- useTempFile(url, sourceFormat)(sendFileFromBot)
    _       <- bot.editLogText(logFile, s"Send file below to @ebook_converter_bot and send me the converted $targetFormat file:")
  } yield ()) |> sendOnErrors({})({
    case ao3Error: Ao3ClientError => s"downloading link from AO3"
    case fileError                => s"getting or uploading file"
  })

  override def onMessage(msg: Message): UIO[BotState] = msg.document match
    case None           => bot.sendText("Not a valid file").map(_ => ExistingFicBotState(state.ficToSend, true))
    case Some(document) => sendToKindle(document)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => StartBotState())

  private def sendToKindle(document: Document) = (for {
    logLink    <- bot.sendText("Getting file link from TG...")
    tgFile     <- bot.getFile(document.fileId)
    url        <- ZIO.succeed(tgFileUrl(tgFile.flatMap(_.filePath).getOrElse("FILE_PATH_NOT_FOUND")))
    logSending <- bot.editLogText(logLink, "Downloading file and sending it to Kindle...")
    _          <- useTempFile(url, targetFormat)(sendFileToEmail)
    _ <- bot.editLogText(
      logSending,
      "Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>"
    )
    patchedRecord <- db.details.patchFicStats(
      state.ficToSend,
      state.ficToSend.details.copy(isOnKindle = true)
    )
  } yield ExistingFicBotState(patchedRecord, true)) |> sendOnError("patching record")

  private def useTempFile(url: String, fileFormat: String)(action: File => Task[Unit]) = {
    def acquire = for {
      file <- ZIO.attempt(File(utils.Config.tempDir + state.ficToSend.fic.title + fileFormat))
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
    ZIO.attempt(MailClient.sendFile(file, state.ficToSend.fic.title + targetFormat)) |> sendOnError({})(
      s"sending file ${file.getName} to kindle email"
    )
