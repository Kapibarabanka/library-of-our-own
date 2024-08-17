package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.bot.Constants.myChatId
import com.kapibarabanka.kapibarabot.bot.MessageData
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.{answerCallbackQuery, sendMessage}
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, Message}
import zio.{Task, UIO, ZIO}

trait Scenario(implicit bot: Api[Task], airtable: AirtableClient, ao3: Ao3):
  protected def startupAction: Task[Unit]
  def onMessage(msg: Message): Task[Scenario]
  def onCallbackQuery(query: CallbackQuery): Task[Scenario]

  def withStartup: Task[Scenario] = startupAction.map(_ => this)

  protected def tryAndSendOnError(
      toTry: ZIO[Any, Throwable, Scenario],
      errorToMessage: PartialFunction[Throwable, String] = { case e => defaultErrorMessage(e) }
  ): UIO[Scenario] =
    toTry.foldZIO(
      error => sendText(errorToMessage.applyOrElse(error, defaultErrorMessage)).map(_ => StartScenario()),
      scenario => ZIO.succeed(scenario)
    )

  private val defaultErrorMessage = (error: Throwable) => s"\nError happened somewhere: ${error.getMessage}"

  protected def sendText(text: String): UIO[Option[Message]] = sendMessageData(MessageData(text))

  protected def sendMessageData(msg: MessageData): UIO[Option[Message]] =
    sendMessage(
      myChatId,
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
    ).exec.foldZIO(
      // TODO: add notification for me here
      e => ZIO.logError("CRITICAL ERROR WHILE SENDING MESSAGE" + e.getMessage).map(_ => None),
      m => ZIO.succeed(Some(m))
    )

  protected def editLogText(startMsg: Option[Message], text: String): UIO[Option[Message]] = startMsg match
    case Some(msg) => editMessage(msg, MessageData(text))
    case None      => sendText(text)

  protected def editLogText(startMsg: Message, text: String): UIO[Option[Message]] = editMessage(startMsg, MessageData(text))

  protected def editMessage(startMsg: Message, msg: MessageData): UIO[Option[Message]] =
    telegramium.bots.high.Methods
      .editMessageText(
        msg.text,
        msg.businessConnectionId,
        Some(myChatId),
        Some(startMsg.messageId),
        msg.inlineMessageId,
        msg.parseMode,
        msg.entities,
        msg.linkPreviewOptions,
        msg.replyMarkup
      )
      .exec
      .foldZIO(
        // TODO: add notification for me here
        e => ZIO.logError("CRITICAL ERROR WHILE SENDING MESSAGE" + e.getMessage).map(_ => None),
        m => ZIO.succeed(Some(m.getOrElse(startMsg)))
      )

  protected def unknownCallbackQuery(query: CallbackQuery): ZIO[Any, Nothing, Unit] =
    answerCallbackQuery(
      callbackQueryId = query.id,
      text = Some(s"You chose ${query.data} and I don't know what to do with it")
    ).exec.foldZIO(
      // TODO: add notification for me here
      e => ZIO.logError("CRITICAL ERROR WHILE SENDING MESSAGE" + e.getMessage),
      _ => ZIO.unit
    )
