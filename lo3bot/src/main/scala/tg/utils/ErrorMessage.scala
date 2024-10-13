package kapibarabanka.lo3.bot
package tg.utils

import ao3scrapper.Ao3Error
import ao3scrapper.Ao3Error.*

object ErrorMessage:
  val noKindleEmail: String  = "Couldn't find kindle email for this user, please notify the bot's author"
  val seriesToKindle: String = "Sorry, can't send series to Kindle yet, please send each work separately"

  def invalidMessage(messageText: String): String = s"'$messageText' is not parsable AO3 link, don't know what to do :c"
  def invalidQuery(queryText: String): String     = s"Don't know how to answer the query $queryText"

  def fromThrowable(error: Throwable, actionName: String): String =
    error match
      case AuthFailed()      => authFailed
      case TooManyRequests() => rateLimit
      case _                 => defaultMessage(error, actionName)

  private def defaultMessage(error: Throwable, actionName: String) = s"\nError happened while $actionName: ${error.getMessage}"

  private val authFailed =
    s"Failed to log in to the Ao3. Please try again in a couple of minutes or check <a href=\"https://x.com/ao3_status\">Ao3 status twitter</a>"

  private val rateLimit =
    "The bot has sent too many requests to the Ao3 and is now being rate-limited. Please try again in a couple of minutes."
