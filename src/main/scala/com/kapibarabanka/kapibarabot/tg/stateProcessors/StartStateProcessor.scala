package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.tg.models.{BotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartStateProcessor(currentState: StartBotState, bot: BotWithChatId)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  override def startup: UIO[Unit] = bot.sendText("Waiting for a fic link or command").unit

  override def onMessage(msg: Message): UIO[BotState] = defaultOnMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => currentState)
