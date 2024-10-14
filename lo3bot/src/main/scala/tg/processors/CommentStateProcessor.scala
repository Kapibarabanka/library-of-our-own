package kapibarabanka.lo3.bot
package tg.processors

import tg.db
import tg.models.{BotState, CommentBotState, ExistingFicBotState, StartBotState}
import tg.services.BotWithChatId

import kapibarabanka.lo3.models.tg.FicComment
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentStateProcessor(state: CommentBotState, bot: BotWithChatId)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): UIO[BotState] =
    addComment(msg.text.getOrElse("")) |> sendOnError(s"adding comment to fic ${state.ficForComment.key}")

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query)

  private def addComment(comment: String) = for {
    logPatching <- bot.sendText("Adding comment...")
    ficWithComment <- db.details.addComment(
      state.ficForComment,
      FicComment(LocalDate.now().toString, comment)
    )
    _ <- bot.editLogText(logPatching, "Successfully added comment!")
  } yield ExistingFicBotState(ficWithComment, true)
