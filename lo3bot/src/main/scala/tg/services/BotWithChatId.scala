package kapibarabanka.lo3.bot
package tg.services

import tg.models.MessageData
import telegramium.bots.{CallbackQuery, File, IFile, Message}
import zio.{Task, UIO}

case class BotWithChatId(chatId: String, botApi: MyBotApi):
  val sendText: String => UIO[Option[Message]] = botApi.sendText(chatId)

  val sendMessage: MessageData => UIO[Option[Message]] = botApi.sendMessage(chatId)

  val editLogText: (Option[Message], String) => UIO[Option[Message]] = botApi.editLogText(chatId)

  val editMessage: (Message, MessageData) => UIO[Option[Message]] = botApi.editMessage(chatId)

  def answerCallbackQuery(query: CallbackQuery, text: Option[String] = None): UIO[Unit] = botApi.answerCallbackQuery(query, text)

  val getFile: String => UIO[Option[File]] = botApi.getFile

  val sendDocument: IFile => Task[Message] = botApi.sendDocument(chatId)
