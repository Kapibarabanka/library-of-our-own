package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.airtable.AirtableError
import com.kapibarabanka.ao3scrapper.models.{Fic, FicType}
import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.bot.*
import com.kapibarabanka.kapibarabot.bot.BotError.*
import com.kapibarabanka.kapibarabot.bot.Buttons.getButtonsForNew
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import iozhik.OpenEnum
import telegramium.bots.*
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.answerCallbackQuery
import telegramium.bots.high.implicits.*
import zio.*
import scalaz.Scalaz.ToIdOps

case class NewFicScenario(link: String)(implicit bot: Api[Task], airtable: AirtableClient, ao3: Ao3) extends Scenario:

  protected override def startupAction: UIO[Unit] = sendMessageData(MessageData(MessageText.newFic(link), replyMarkup = getButtonsForNew)).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = {
    query.data match
      case Buttons.addToAirtable.callbackData => onSave(query)
      case _                                  => unknownCallbackQuery(query).map(_ => this)
  }

  private def onSave(query: CallbackQuery) = {
    query.message
      .collect { case startMsg: Message =>
        (for {
          _          <- answerCallbackQuery(callbackQueryId = query.id, text = Some("Working on it...")).exec
          logParsing <- editLogText(startMsg, "Parsing AO3...")
          ficLink <- startMsg.entities.collectFirst { case OpenEnum.Known(TextLinkMessageEntity(_, _, url)) => url } match
            case Some(value) => ZIO.succeed(value)
            case None        => ZIO.fail(NoLinkInMessage())
          fic          <- getFicByLink(ficLink)
          savingMsg    <- editLogText(logParsing, "Saving to Airtable...")
          record       <- airtable.addFic(fic)
          _            <- editLogText(savingMsg, "Enjoy:")
          nextScenario <- ExistingFicScenario(record).withStartup
        } yield nextScenario) |> tryAndSendOnError({
          case ao3Error: Ao3Error => s"\nOh no, couldn't get fic from Ao3: ${ao3Error.getMessage}"
          case airtableError: AirtableError => s"\nOh no, couldn't save to Airtable: ${airtableError.getMessage}"
        })
      }
      .getOrElse(ZIO.succeed(this))
  }

  private def getFicByLink(link: String): ZIO[Any, InvalidFicLink | Ao3Error, Fic] = Ao3Url.tryParseFicId(link) match
    case Some((FicType.Work, id))   => ao3.work(id).mapError(e => Ao3Error(e.getMessage))
    case Some((FicType.Series, id)) => ao3.series(id).mapError(e => Ao3Error(e.getMessage))
    case None                       => ZIO.fail(InvalidFicLink(link))
