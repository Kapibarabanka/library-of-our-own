package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.tg.models.{BotState, FeedbackBotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.services.{AdminBot, BotWithChatId}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.UIO

case class FeedbackStateProcessor(state: FeedbackBotState, bot: BotWithChatId)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] = bot.sendText("Send me some feedback, encountered problems or ideas for new features:").unit

  override def onMessage(msg: Message): UIO[BotState] =
    val action = for {
      _ <- AdminBot.feedback(bot.chatId, msg.chat.username, msg.text.getOrElse("empty message"))
      _ <- bot.sendText("Thank you!")
    } yield StartBotState(true)
    action |> sendOnError(s"getting feedback from user ${bot.chatId}")

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query)
