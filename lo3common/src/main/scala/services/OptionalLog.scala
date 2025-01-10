package kapibarabanka.lo3.common
package services

import telegramium.bots.Message
import zio.{UIO, ZIO}

sealed trait OptionalLog:
  def edit(logText: String): UIO[Unit]
  def delete: UIO[Unit]

case class LogMessage(api: MyBotApi, chatId: String, message: Option[Message]) extends OptionalLog:
  def edit(logText: String): UIO[Unit] = for {
    _ <- ZIO.log(logText)
    _ <- api.editLogText(chatId)(message, logText).unit
  } yield ()

  def delete: UIO[Unit] = if (message.nonEmpty) api.deleteMessage(chatId)(message.get) else ZIO.unit

object LogMessage:
  def create(logText: String, api: MyBotApi, chatId: String): UIO[OptionalLog] = for {
    message <- api.sendText(chatId)(logText)
  } yield LogMessage(api, chatId, message)

case class EmptyLog() extends OptionalLog:
  override def edit(logText: String): UIO[Unit] = ZIO.unit

  override def delete: UIO[Unit] = ZIO.unit
