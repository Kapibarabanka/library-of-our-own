package com.kapibarabanka.kapibarabot.sqlite

enum SqliteError(cause: Throwable) extends Exception(cause):
  case DbInitError(cause: Throwable)     extends SqliteError(cause)
  case DbActionError(cause: Throwable)   extends SqliteError(cause)
  case CantConnectToDb(cause: Throwable) extends SqliteError(cause)
