package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.kapibarabot.domain.FicComment
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentStateProcessor(state: CommentBotState, bot: BotWithChatId, db: FanficDbOld)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): UIO[BotState] =
    (for {
      logPatching    <- bot.sendText("Adding comment...")
      ficWithComment <- db.addComment(state.ficForComment, FicComment(LocalDate.now().toString, msg.text.getOrElse("")))
      _              <- bot.editLogText(logPatching, "Successfully added comment!")
    } yield ExistingFicBotState(ficWithComment, true)) |> sendOnError(s"adding comment to fic ${state.ficForComment.key}")

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => StartBotState())
