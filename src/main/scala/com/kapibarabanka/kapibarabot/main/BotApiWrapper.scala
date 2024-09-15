package com.kapibarabanka.kapibarabot.main

import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.*
import telegramium.bots.high.implicits.*
import zio.*

class BotApiWrapper(chatId: ChatIntId)(implicit bot: Api[Task]):
  def sendText(text: String): UIO[Option[Message]] = sendMessage(MessageData(text))

  def sendMessage(msg: MessageData): UIO[Option[Message]] =
    telegramium.bots.high.Methods
      .sendMessage(
        chatId,
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

  def editLogText(startMsg: Option[Message], text: String): UIO[Option[Message]] = startMsg match
    case Some(msg) => editMessage(msg, MessageData(text))
    case None      => sendText(text)

  def editLogText(startMsg: Message, text: String): UIO[Option[Message]] = editMessage(startMsg, MessageData(text))

  def editMessage(startMsg: Message, msg: MessageData): UIO[Option[Message]] =
    telegramium.bots.high.Methods
      .editMessageText(
        msg.text,
        msg.businessConnectionId,
        Some(chatId),
        Some(startMsg.messageId),
        msg.inlineMessageId,
        msg.parseMode,
        msg.entities,
        msg.linkPreviewOptions,
        msg.replyMarkup
      )
      .exec |> logCritical("EDITING MESSAGE") map (res => res.map(_.getOrElse(startMsg)))

  def answerCallbackQuery(query: CallbackQuery, text: Option[String] = None): UIO[Unit] =
    telegramium.bots.high.Methods
      .answerCallbackQuery(
        callbackQueryId = query.id,
        text = text
      )
      .exec |> logCritical(s"ANSWERING QUERY (id: ${query.id}, data: ${query.data})") map (_ => ())

  def getFile(id: String): UIO[Option[File]] =
    telegramium.bots.high.Methods.getFile(id).exec |> logCritical(s"GETTING FILE WITH ID $id")

  def sendDocument(file: IFile) =
    telegramium.bots.high.Methods.sendDocument(chatId, file).exec

  private def logCritical[T](actionName: String)(action: Task[T]): UIO[Option[T]] =
    action.foldZIO(
      // TODO: add notification for me here
      error => ZIO.logError(s"CRITICAL ERROR WHILE $actionName" + error.getMessage).map(_ => None),
      result => ZIO.succeed(Some(result))
    )
