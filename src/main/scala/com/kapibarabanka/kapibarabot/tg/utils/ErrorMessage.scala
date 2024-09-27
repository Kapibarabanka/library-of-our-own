package com.kapibarabanka.kapibarabot.tg.utils

object ErrorMessage:
  val noKindleEmail: String  = "Couldn't find kindle email for this user, please notify the bot's author"
  val seriesToKindle: String = "Sorry, can't send series to Kindle yet, please send each work separately"

  def invalidMessage(messageText: String): String = s"'$messageText' is not parsable AO3 link, don't know what to do :c"
  def invalidQuery(queryText: String): String     = s"Don't know how to answer the query $queryText"

  def fromThrowable(error: Throwable, actionName: String) = s"\nError happened while $actionName: ${error.getMessage}"
