package com.kapibarabanka.kapibarabot.utils

import com.kapibarabanka.kapibarabot.domain.MyFicStats
import telegramium.bots.{InlineKeyboardButton, InlineKeyboardMarkup}

object Buttons:
  def getButtonsForExisting(stats: MyFicStats): Option[InlineKeyboardMarkup] = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        List(
          if (stats.backlog) None else Some(addToBacklog),
          if (stats.isOnKindle || stats.kindleToDo) None else Some(sendToKindle)
        ).flatten,
        List(
          if (stats.isReadToday) None else Some(markAsReadToday),
          if (stats.read || stats.backlog) None else Some(markAsRead),
          if (stats.fire) Some(rateNotFire) else Some(rateFire)
        ).flatten,
        if (stats.read) List(rateNever, rateMeh, rateOk, rateNice, rateBrilliant) else List(),
        List(addComment)
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
  val addToBacklog      = InlineKeyboardButton(s"${Emoji.backlog} Add to backlog", callbackData = Some("addToBacklog"))
  val removeFromBacklog = InlineKeyboardButton(s"${Emoji.cross} Remove from backlog", callbackData = Some("removeFromBacklog"))
  val sendToKindle      = InlineKeyboardButton(s"${Emoji.kindle} Send to Kindle", callbackData = Some("sendToKindle"))
  val kindleToDo        = InlineKeyboardButton(s"${Emoji.kindle} Kindle ToDo", callbackData = Some("kindleToDo"))
  val removeFromKindle  = InlineKeyboardButton(s"${Emoji.kindle} Remove from Kindle", callbackData = Some("removeFromKindle"))
  val markAsReadToday   = InlineKeyboardButton(s"${Emoji.read} Read today", callbackData = Some("markAsReadToday"))
  val markAsRead        = InlineKeyboardButton(s"${Emoji.question} Read some time ago", callbackData = Some("markAsRead"))
  val addComment        = InlineKeyboardButton(s"${Emoji.comment} Add comment", callbackData = Some("addComment"))

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
