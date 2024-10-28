package kapibarabanka.lo3.bot
package processors

import models.{BotState, CommentBotState, ExistingFicBotState}
import services.Lo3Api

import kapibarabanka.lo3.common.models.domain.FicComment
import kapibarabanka.lo3.common.openapi.FicDetailsClient
import kapibarabanka.lo3.common.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentStateProcessor(state: CommentBotState, bot: BotWithChatId)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] =
    addComment(msg.text.getOrElse("")) |> sendOnError(s"adding comment to fic ${state.ficForComment.key}")

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query)

  private def addComment(comment: String) = for {
    logPatching <- bot.sendText("Adding comment...")
    ficWithComment <- Lo3Api.run(FicDetailsClient.addComment(state.ficForComment.key, FicComment(LocalDate.now().toString, comment)))
    _ <- bot.editLogText(logPatching, "Successfully added comment!")
  } yield ExistingFicBotState(ficWithComment, true)
