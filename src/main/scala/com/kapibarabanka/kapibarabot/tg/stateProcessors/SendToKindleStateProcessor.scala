package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Error}
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.tg.db
import com.kapibarabanka.kapibarabot.tg.models.{BotState, ExistingFicBotState, SendToKindleBotState, StartBotState}
import com.kapibarabanka.kapibarabot.{AppConfig, utils}
import com.kapibarabanka.kapibarabot.utils.MailClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, Document, InputPartFile, Message}
import zio.*

import java.io.File
import java.net.URL
import scala.language.postfixOps
import scala.sys.process.*

case class SendToKindleStateProcessor(state: SendToKindleBotState, bot: BotWithChatId, ao3: Ao3)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot),
      WithTempFiles(bot):
  private val sourceFormat = ".mobi"
  private val targetFormat = ".epub"

  override def startup: UIO[Unit] =
    val action = for {
      logLink <- bot.sendText("Getting link from AO3...")
      url     <- ao3.getDownloadLink(state.ficToSend.fic.id)
      logFile <- bot.editLogText(logLink, s"Uploading $sourceFormat file...")
      _       <- useTempFile(state.ficToSend.fic.title + sourceFormat)(fromUrlToUser(url))
      _ <- bot.editLogText(logFile, s"Send file below to @ebook_converter_bot and send me the converted $targetFormat file:")
    } yield ()
    action |> sendOnError({})("getting download link from AO3")

  override def onMessage(msg: Message): UIO[BotState] = msg.document match
    case None           => bot.sendText("Not a valid file").map(_ => ExistingFicBotState(state.ficToSend, true))
    case Some(document) => sendToKindle(document)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => StartBotState())

  private def sendToKindle(document: Document) =
    val action = for {
      logLink    <- bot.sendText("Getting file link from TG...")
      tgFile     <- bot.getFile(document.fileId)
      url        <- ZIO.succeed(tgFileUrl(tgFile.flatMap(_.filePath).getOrElse("FILE_PATH_NOT_FOUND")))
      logSending <- bot.editLogText(logLink, "Downloading file and sending it to Kindle...")
      _          <- useTempFile(state.ficToSend.fic.title + targetFormat)(fromUrlToEmail(url))
      _ <- bot.editLogText(
        logSending,
        "Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>"
      )
      patchedRecord <- db.details.patchFicDetails(
        state.ficToSend,
        state.ficToSend.details.copy(isOnKindle = true)
      )
    } yield ExistingFicBotState(patchedRecord, true)
    action |> sendOnError(s"marking fic ${state.ficToSend.key} as sent to Kindle in DB")

  private def fromUrlToUser(url: String)(file: File) = for {
    _ <- ZIO.log(s"Downloading file form $url")
    _ <- ZIO.attempt(new URL(url) #> file !!) |> sendOnError({})(s"downloading file from $url")
    _ <- ZIO.log(s"Finished download form $url")
    _ <- bot.sendDocument(InputPartFile(file)).unit |> sendOnError({})(s"uploading file ${file.getName}")
  } yield ()

  private def fromUrlToEmail(url: String)(file: File) = for {
    _ <- ZIO.log(s"Downloading file form $url")
    _ <- ZIO.attempt(new URL(url) #> file !!) |> sendOnError({})(s"downloading file from $url")
    _ <- ZIO.log(s"Finished download form $url")
    _ <- ZIO.attempt(MailClient.sendFile(file, state.ficToSend.fic.title + targetFormat, state.userEmail)) |> sendOnError({})(
      s"sending file ${file.getName} to kindle email"
    )
  } yield ()

  private def tgFileUrl(filePath: String): String = s"https://api.telegram.org/file/bot${AppConfig.tgToken}/$filePath"
