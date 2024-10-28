package kapibarabanka.lo3.bot
package processors

import models.{BotState, StartBotState}
import services.Lo3Api

import kapibarabanka.lo3.common.services.BotWithChatId
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartStateProcessor(currentState: StartBotState, bot: BotWithChatId)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  override def startup: UIO[Unit] = bot.sendText("Waiting for a fic link or command").unit

  override def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] = defaultOnMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => currentState)
