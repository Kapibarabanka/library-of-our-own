package com.kapibarabanka.kapibarabot.utils

import com.kapibarabanka.kapibarabot.domain.MyFicStats
import com.kapibarabanka.kapibarabot.main.Emoji
import telegramium.bots.{InlineKeyboardButton, InlineKeyboardMarkup}

object Buttons:
  def getButtonsForExisting(stats: MyFicStats) = Some(
    InlineKeyboardMarkup(inlineKeyboard =
      List(
        if (stats.backlog) List() else List(addToBacklog),
        if (stats.isOnKindle || stats.kindleToDo) List() else List(sendToKindle),
        if (stats.isReadToday) List() else List(markAsReadToday),
        (if (stats.fire) rateNotFire else rateFire) :: (if (stats.read)
                                                          List(rateNever, rateMeh, rateOk, rateNice, rateBrilliant)
                                                        else List()),
        if (stats.read || stats.backlog) List() else List(markAsRead),
        List(addComment)
      )
    )
  )

  def getButtonsForNew = Some(
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
