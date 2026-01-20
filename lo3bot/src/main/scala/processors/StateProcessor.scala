package kapibarabanka.lo3.bot
package processors

import models.*
import services.Lo3Api
import utils.{ErrorMessage, MessageText}

import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType}
import kapibarabanka.lo3.common.models.tg.TgError.InaccessibleMessageError
import kapibarabanka.lo3.common.lo3api.{FicsClient, UserClient}
import kapibarabanka.lo3.common.services.{BotWithChatId, Utils}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, InputPartFile, Message}
import zio.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

trait StateProcessor(currentState: BotState, bot: BotWithChatId) extends WithErrorHandling:
  def startup: ZIO[Lo3Api, Nothing, Unit]
  def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState]
  def onCallbackQuery(query: CallbackQuery): ZIO[Lo3Api, Nothing, BotState]

  protected def defaultOnMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] = for {
    messageText <- ZIO.succeed(msg.text.getOrElse("NO_TEXT"))
    nextState   <- getNextState(messageText)
  } yield nextState

  protected def sendOnError[R](actionName: String)(action: ZIO[R, Throwable, BotState]): ZIO[R, Nothing, BotState] =
    sendOnError(StartBotState(true))(actionName)(action)

  protected def sendOnErrors[R](errorToMessage: PartialFunction[Throwable, String])(
      action: ZIO[R, Throwable, BotState]
  ): ZIO[R, Nothing, BotState] =
    sendOnErrors(StartBotState(true))(errorToMessage)(action)

  protected def unknownCallbackQuery(query: CallbackQuery): UIO[StartBotState] =
    bot
      .answerCallbackQuery(
        query,
        Some(ErrorMessage.invalidQuery(query.data.getOrElse("")))
      )
      .map(_ => StartBotState(true))

  protected def tryGetMessage(query: CallbackQuery): ZIO[Any, InaccessibleMessageError, Message] =
    query.message.collect { case msg: Message => msg } match
      case Some(message) => ZIO.succeed(message)
      case None          => ZIO.fail(InaccessibleMessageError())

  private def getNextState(text: String) =
    text match
      case "/backlog"         => backLogCommand()
      case "/help" | "/start" => bot.sendText(MessageText.help).map(_ => StartBotState(true))
      case "/set_email"       => ZIO.succeed(SetEmailBotState())
      case "/feedback"        => ZIO.succeed(FeedbackBotState())
      case _                  => getStateWithFic(text)

  private def getStateWithFic(ficLink: String): ZIO[Lo3Api, Nothing, BotState] =
    val action = for {
      record <- Lo3Api.run(FicsClient.getFicByLink(ficLink, bot.chatId, true))
    } yield ExistingFicBotState(record, true)
    action |> sendOnError(s"getting user record (${bot.chatId} -- $ficLink) in DB")

  private def backLogCommand() =
    val action = for {
      backlog <- Lo3Api.run(UserClient.backlog(bot.chatId, true))
      log     <- bot.editLogText(None, "Uploading file...")
      _       <- Utils.useTempFile("backlog.html")(sendBacklog(backlog))
      log     <- bot.editLogText(log, "Enjoy:")
    } yield StartBotState(true)
    action |> sendOnError(s"getting user backlog")

  private def sendBacklog(backlog: String)(file: File) = {
    Files.write(file.toPath, backlog.getBytes(StandardCharsets.UTF_8))
    bot.sendDocument(InputPartFile(file)).unit |> sendOnError({})(s"uploading file ${file.getName}")
  }
