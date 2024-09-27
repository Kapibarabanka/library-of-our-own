package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.domain.{FicType, Series, Work}
import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.utils.Ao3Url
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.tg.BotError.*
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.tg.models.{BotState, ExistingFicBotState, MessageData, NewFicBotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.utils.{Buttons, MessageText}
import com.kapibarabanka.kapibarabot.tg.utils.Buttons.getButtonsForNew
import iozhik.OpenEnum
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import scala.language.postfixOps

case class NewFicStateProcessor(currentState: NewFicBotState, bot: BotWithChatId, ao3: Ao3, db: DbService)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.newFic(currentState.ao3Link), replyMarkup = getButtonsForNew)).unit

  override def onMessage(msg: Message): UIO[BotState] = StartStateProcessor(StartBotState(), bot, db).onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = {
    query.data match
      case Buttons.parseAndSave.callbackData => onSave(query)
      case _                                 => unknownCallbackQuery(query).map(_ => currentState)
  }

  private def onSave(query: CallbackQuery) = {
    query.message
      .collect { case startMsg: Message =>
        (for {
          _          <- bot.answerCallbackQuery(query, Some("Working on it..."))
          logParsing <- bot.editLogText(Some(startMsg), "Parsing AO3...")
          ficLink <- startMsg.entities.collectFirst { case OpenEnum.Known(TextLinkMessageEntity(_, _, url)) => url } match
            case Some(value) => ZIO.succeed(value)
            case None        => ZIO.fail(NoLinkInMessage())
          ficFromAo3 <- getFicByLink(ficLink)
          savingMsg  <- bot.editLogText(logParsing, "Saving to database...")
          flatFic <- ficFromAo3 match
            case work: Work     => db.fics.add(work)
            case series: Series => db.fics.add(series)
          record <- db.details.getOrCreateUserFic(UserFicKey(bot.chatId, flatFic.id, flatFic.ficType))
          _      <- bot.editLogText(savingMsg, "Enjoy:")
        } yield ExistingFicBotState(record, true)) |> sendOnErrors({
          case ao3Error: Ao3Error => s"getting fic from Ao3"
          case _                  => s"adding fic to db"
        })
      }
      .getOrElse(ZIO.succeed(currentState))
  }

  private def getFicByLink(link: String): ZIO[Any, InvalidFicLink | Ao3Error, Work | Series] =
    Ao3Url.tryParseFicId(link) match
      case Some((FicType.Work, id))   => ao3.work(id).mapError(e => Ao3Error(e.getMessage))
      case Some((FicType.Series, id)) => ao3.series(id).mapError(e => Ao3Error(e.getMessage))
      case None                       => ZIO.fail(InvalidFicLink(link))
