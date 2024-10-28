package kapibarabanka.lo3.bot
package processors

import models.{BotState, SetEmailBotState, StartBotState}
import services.Lo3Api

import kapibarabanka.lo3.common.openapi.UserClient
import kapibarabanka.lo3.common.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.{UIO, ZIO}

case class SetEmailStateProcessor(state: SetEmailBotState, bot: BotWithChatId)
    extends StateProcessor(state, bot),
      WithErrorHandling(bot):
  // TODO: add detailed instructions
  override def startup: UIO[Unit] = bot.sendText("Send me your Kindle email:").unit

  override def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] =
    val text = msg.text.getOrElse("").trim
    if (validKindleEmail(text))
      setEmail(text) |> sendOnError(s"setting email for user ${bot.chatId}")
    else
      bot.sendText(s"$text is not a valid Kindle email").map(_ => StartBotState(true))

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query)

  private def setEmail(email: String) = for {
    logPatching    <- bot.sendText("Setting email...")
    ficWithComment <- Lo3Api.run(UserClient.setEmail(bot.chatId, email))
    _              <- bot.editLogText(logPatching, "Successfully set email!")
  } yield StartBotState(true)

  private def validKindleEmail(text: String) = text.endsWith("@kindle.com")
