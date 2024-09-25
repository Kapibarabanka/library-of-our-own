package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.airtable.AirtableError
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.main.BotError.*
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, MessageData, WithErrorHandling}
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.Buttons.getButtonsForNew
import com.kapibarabanka.kapibarabot.utils.{Buttons, MessageText}
import iozhik.OpenEnum
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import scala.language.postfixOps

case class NewFicScenario(link: String)(implicit bot: BotApiWrapper, airtable: AirtableClient, ao3: Ao3, db: FanficDbOld)
    extends Scenario,
      WithErrorHandling(bot):

  protected override def startupAction: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.newFic(link), replyMarkup = getButtonsForNew)).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = {
    query.data match
      case Buttons.parseAndSave.callbackData => onSave(query)
      case _                                 => unknownCallbackQuery(query).map(_ => this)
  }

  private def onSave(query: CallbackQuery) = {
    query.message
      .collect { case startMsg: Message =>
        (for {
          _          <- bot.answerCallbackQuery(query, text = Some("Working on it..."))
          logParsing <- bot.editLogText(startMsg, "Parsing AO3...")
          ficLink <- startMsg.entities.collectFirst { case OpenEnum.Known(TextLinkMessageEntity(_, _, url)) => url } match
            case Some(value) => ZIO.succeed(value)
            case None        => ZIO.fail(NoLinkInMessage())
          ficFromAo3 <- getFicByLink(ficLink)
          savingMsg  <- bot.editLogText(logParsing, "Saving to database...")
          flatFic <- ficFromAo3 match
            case work: Work     => db.add(work)
            case series: Series => db.add(series)
          record       <- db.getOrCreateUserFic(UserFicKey(bot.chatId, flatFic.id, flatFic.ficType))
          _            <- bot.editLogText(savingMsg, "Enjoy:")
          nextScenario <- ExistingFicScenario(record).withStartup
        } yield nextScenario) |> sendOnErrors({
          case ao3Error: Ao3Error           => s"getting fic from Ao3"
          case airtableError: AirtableError => s"adding fic to db"
        })
      }
      .getOrElse(ZIO.succeed(this))
  }

  private def getFicByLink(link: String): ZIO[Any, InvalidFicLink | Ao3Error, Work | Series] =
    Ao3Url.tryParseFicId(link) match
      case Some((FicType.Work, id))   => ao3.work(id).mapError(e => Ao3Error(e.getMessage))
      case Some((FicType.Series, id)) => ao3.series(id).mapError(e => Ao3Error(e.getMessage))
      case None                       => ZIO.fail(InvalidFicLink(link))
