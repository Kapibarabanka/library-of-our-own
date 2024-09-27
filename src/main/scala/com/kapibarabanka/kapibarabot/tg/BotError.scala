package com.kapibarabanka.kapibarabot.tg

enum BotError(msg: String) extends Exception(msg):
  case InvalidFicLink(link: String) extends BotError(s"$link is not a valid work or series link")
  case Ao3Error(msg: String)        extends BotError(msg)
  case NoLinkInMessage()            extends BotError("Couldn't find link in message")
  case Critical(msg: String)        extends BotError(msg)
