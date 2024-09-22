package com.kapibarabanka.kapibarabot.utils

import com.kapibarabanka.kapibarabot.domain.MyFicStats
import telegramium.bots.{InlineKeyboardButton, InlineKeyboardMarkup}

object Buttons:
  def getButtonsForExisting(stats: MyFicStats): Option[InlineKeyboardMarkup] = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        List(
          if (stats.backlog) None else Some(addToBacklog),
          if (stats.isOnKindle) None else Some(sendToKindle)
        ).flatten,
        List(
          if (stats.read || stats.backlog) None else Some(markAsRead),
          Some(markAsStartedToday),
          Some(markAsFinishedToday)
        ).flatten,
        if (stats.read) List(rateNever, rateMeh, rateOk, rateNice, rateBrilliant) else List(),
        List(addComment, if (stats.fire) rateNotFire else rateFire)
      )
    )
  )

  def getButtonsForNew: Option[InlineKeyboardMarkup] = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        List(addToAirtable)
      )
    )
  )

  // for existing
  val addToBacklog        = InlineKeyboardButton(s"${Emoji.backlog} Add to backlog", callbackData = Some("addToBacklog"))
  val removeFromBacklog   = InlineKeyboardButton(s"${Emoji.cross} Remove from backlog", callbackData = Some("removeFromBacklog"))
  val sendToKindle        = InlineKeyboardButton(s"${Emoji.kindle} Send to Kindle", callbackData = Some("sendToKindle"))
  val removeFromKindle    = InlineKeyboardButton(s"${Emoji.kindle} Remove from Kindle", callbackData = Some("removeFromKindle"))
  val markAsStartedToday  = InlineKeyboardButton(s"${Emoji.start} Started today", callbackData = Some("markAsStartedToday"))
  val markAsFinishedToday = InlineKeyboardButton(s"${Emoji.finish} Finished today", callbackData = Some("markAsFinishedToday"))
  val markAsRead          = InlineKeyboardButton(s"${Emoji.question} Read some time ago", callbackData = Some("markAsRead"))
  val addComment          = InlineKeyboardButton(s"${Emoji.comment} Add comment", callbackData = Some("addComment"))

  // rating
  val rateNever     = InlineKeyboardButton(s"${Emoji.never}", callbackData = Some("rateNever"))
  val rateMeh       = InlineKeyboardButton(s"${Emoji.meh}", callbackData = Some("rateMeh"))
  val rateOk        = InlineKeyboardButton(s"${Emoji.ok}", callbackData = Some("rateOk"))
  val rateNice      = InlineKeyboardButton(s"${Emoji.nice}", callbackData = Some("rateNice"))
  val rateBrilliant = InlineKeyboardButton(s"${Emoji.brilliant}", callbackData = Some("rateBrilliant"))
  val rateFire      = InlineKeyboardButton(s"${Emoji.fire}", callbackData = Some("rateFire"))
  val rateNotFire   = InlineKeyboardButton(s"${Emoji.notFire}", callbackData = Some("rateNotFire"))

  // for new
  val addToAirtable = InlineKeyboardButton(s"${Emoji.airtable} Save", callbackData = Some("addToAirtable"))
