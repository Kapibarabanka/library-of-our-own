package kapibarabanka.lo3.common
package services

import telegramium.bots.Message
import zio.{UIO, ZIO}

case class OptionalLog(api: MyBotApi, chatId: String, needToLog: Boolean, message: Option[Message]):
  def edit(logText: String): UIO[Unit] = if (needToLog) api.editLogText(chatId)(message, logText).unit else ZIO.unit

  def delete: UIO[Unit] = if (needToLog && message.nonEmpty) api.deleteMessage(chatId)(message.get) else ZIO.unit

object OptionalLog:
  def create(logText: String, api: MyBotApi, chatId: String, needToLog: Boolean): UIO[OptionalLog] = for {
    message <- if (needToLog) api.sendText(chatId)(logText) else ZIO.succeed(None)
  } yield OptionalLog(api, chatId, needToLog, message)
