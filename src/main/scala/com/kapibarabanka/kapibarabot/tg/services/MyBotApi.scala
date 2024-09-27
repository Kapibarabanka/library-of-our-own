package com.kapibarabanka.kapibarabot.tg.services

import com.kapibarabanka.kapibarabot.tg.models.MessageData
import com.kapibarabanka.kapibarabot.tg.TgError.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import telegramium.bots.high.{Api, BotApi}
import telegramium.bots.high.Methods.*
import telegramium.bots.high.implicits.*
import zio.*
import zio.interop.catz.*

trait MyBotApi:
  val baseApi: Api[Task]
  def sendText(chatId: String)(text: String): UIO[Option[Message]]
  def sendMessage(chatId: String)(msg: MessageData): UIO[Option[Message]]
  def editLogText(chatId: String)(startMsg: Option[Message], text: String): UIO[Option[Message]]
  def editMessage(chatId: String)(startMsg: Message, msg: MessageData): UIO[Option[Message]]
  def answerCallbackQuery(query: CallbackQuery, text: Option[String] = None): UIO[Unit]
  def getFile(id: String): UIO[Option[File]]
  def sendDocument(chatId: String)(file: IFile): IO[CantSendDocument, Message]

case class MyBotApiImpl(baseApi: Api[Task]) extends MyBotApi:
  implicit val botImplicit: Api[Task] = baseApi

  override def sendText(chatId: String)(text: String): UIO[Option[Message]] = sendMessage(chatId)(MessageData(text))

  override def sendMessage(chatId: String)(msg: MessageData): UIO[Option[Message]] =
    telegramium.bots.high.Methods
      .sendMessage(
        ChatStrId(chatId),
        msg.text,
        msg.businessConnectionId,
        msg.messageThreadId,
        msg.parseMode,
        msg.entities,
        msg.linkPreviewOptions,
        msg.disableNotification,
        msg.protectContent,
        msg.messageEffectId,
        msg.replyParameters,
        msg.replyMarkup
      )
      .exec |> logCritical("SENDING MESSAGE")

  override def editLogText(chatId: String)(startMsg: Option[Message], text: String): UIO[Option[Message]] = startMsg match
    case Some(msg) => editMessage(chatId)(msg, MessageData(text))
    case None      => sendText(chatId)(text)

  override def editMessage(chatId: String)(startMsg: Message, msg: MessageData): UIO[Option[Message]] =
    telegramium.bots.high.Methods
      .editMessageText(
        msg.text,
        msg.businessConnectionId,
        Some(ChatStrId(chatId)),
        Some(startMsg.messageId),
        msg.inlineMessageId,
        msg.parseMode,
        msg.entities,
        msg.linkPreviewOptions,
        msg.replyMarkup
      )
      .exec |> logCritical("EDITING MESSAGE") map (res => res.map(_.getOrElse(startMsg)))

  override def answerCallbackQuery(query: CallbackQuery, text: Option[String] = None): UIO[Unit] =
    telegramium.bots.high.Methods
      .answerCallbackQuery(
        callbackQueryId = query.id,
        text = text
      )
      .exec |> logCritical(s"ANSWERING QUERY (id: ${query.id}, data: ${query.data})") map (_ => ())

  override def getFile(id: String): UIO[Option[File]] =
    telegramium.bots.high.Methods.getFile(id).exec |> logCritical(s"GETTING FILE WITH ID $id")

  override def sendDocument(chatId: String)(file: IFile): IO[CantSendDocument, Message] =
    telegramium.bots.high.Methods.sendDocument(ChatStrId(chatId), file).exec.mapError(CantSendDocument(_))

  private def logCritical[T](actionName: String)(action: Task[T]): UIO[Option[T]] =
    action.foldZIO(
      // TODO: add notification for me here
      error => ZIO.logError(s"[Kapibarabot] CRITICAL ERROR WHILE $actionName" + error.getMessage).map(_ => None),
      result => ZIO.succeed(Some(result))
    )

object MyBotApiImpl:
  def layer(baseUrl: String): ZLayer[Any & Scope, Throwable, MyBotApiImpl] = ZLayer {
    for {
      catsHttpClient       <- BlazeClientBuilder[Task].resource.toScopedZIO
      catsClientWithLogger <- ZIO.succeed(Logger(logBody = true, logHeaders = true)(catsHttpClient))
    } yield MyBotApiImpl(BotApi(catsClientWithLogger, baseUrl = baseUrl))
  }
