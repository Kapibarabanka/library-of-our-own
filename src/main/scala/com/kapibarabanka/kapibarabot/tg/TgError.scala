package com.kapibarabanka.kapibarabot.tg

enum TgError(cause: Throwable) extends Exception(cause):
  case InaccessibleMessageError() extends TgError(Exception(s"Couldn't access message"))
  case CantSendDocument(cause: Throwable) extends TgError(cause)
