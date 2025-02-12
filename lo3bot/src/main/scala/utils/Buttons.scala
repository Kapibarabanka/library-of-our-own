package kapibarabanka.lo3.bot
package utils

import kapibarabanka.lo3.common.models.domain.{ReadDatesInfo, UserFicRecord}
import kapibarabanka.lo3.common.models.tg.*
import telegramium.bots.{InlineKeyboardButton, InlineKeyboardMarkup}

object Buttons:
  def getButtonsForExisting(fic: UserFicRecord): Option[InlineKeyboardMarkup] = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        List(
          if (fic.details.backlog) Some(removeFromBacklog) else Some(addToBacklog),
          if (fic.details.isOnKindle) None else Some(sendToKindle)
        ).flatten,
        getDatesButtons(fic.readDatesInfo),
        List(rateNever, rateMeh, rateOk, rateNice, rateBrilliant),
        List(addComment, update, if (fic.details.spicy) rateNotSpicy else rateSpicy)
      )
    )
  )

  def getButtonsForNew: Option[InlineKeyboardMarkup] = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        List(parseAndSave)
      )
    )
  )

  private def getDatesButtons(info: ReadDatesInfo): List[InlineKeyboardButton] =
    List(
      if (info.canAddStart) Some(markAsStartedToday) else None,
      if (info.canCancelStart) Some(cancelStartedToday) else None,
      if (info.canAddFinish) Some(markAsFinishedToday) else None,
      if (info.canCancelFinish) Some(cancelFinishedToday) else None
    ).flatten

  // for existing
  val addToBacklog      = InlineKeyboardButton(s"${Emoji.backlog} Add to backlog", callbackData = Some("addToBacklog"))
  val removeFromBacklog = InlineKeyboardButton(s"${Emoji.cross} Remove from backlog", callbackData = Some("removeFromBacklog"))
  val sendToKindle      = InlineKeyboardButton(s"${Emoji.kindle} Send to Kindle", callbackData = Some("sendToKindle"))

  val addComment = InlineKeyboardButton(s"${Emoji.comment} Add note", callbackData = Some("addComment"))
  val update     = InlineKeyboardButton(s"${Emoji.refresh} Update", callbackData = Some("update"))

  // read dates
  val markAsRead          = InlineKeyboardButton(s"${Emoji.question} Read some time ago", callbackData = Some("markAsRead"))
  val markAsStartedToday  = InlineKeyboardButton(s"${Emoji.start} Started today", callbackData = Some("markAsStartedToday"))
  val markAsFinishedToday = InlineKeyboardButton(s"${Emoji.finish} Finished today", callbackData = Some("markAsFinishedToday"))
  val cancelStartedToday  = InlineKeyboardButton(s"${Emoji.cross} Not started today", callbackData = Some("cancelStartedToday"))
  val cancelFinishedToday = InlineKeyboardButton(s"${Emoji.cross} Not finished today", callbackData = Some("cancelFinishedToday"))

  // rating
  val rateNever     = InlineKeyboardButton(s"${Emoji.never}", callbackData = Some("rateNever"))
  val rateMeh       = InlineKeyboardButton(s"${Emoji.meh}", callbackData = Some("rateMeh"))
  val rateOk        = InlineKeyboardButton(s"${Emoji.ok}", callbackData = Some("rateOk"))
  val rateNice      = InlineKeyboardButton(s"${Emoji.nice}", callbackData = Some("rateNice"))
  val rateBrilliant = InlineKeyboardButton(s"${Emoji.brilliant}", callbackData = Some("rateBrilliant"))
  val rateSpicy     = InlineKeyboardButton(s"${Emoji.fire}", callbackData = Some("rateSpicy"))
  val rateNotSpicy  = InlineKeyboardButton(s"${Emoji.notFire}", callbackData = Some("rateNotSpicy"))

  // for new
  val parseAndSave = InlineKeyboardButton(s"${Emoji.airtable} Parse and save to db", callbackData = Some("parseAndSave"))
