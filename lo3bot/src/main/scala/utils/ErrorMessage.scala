package kapibarabanka.lo3.bot
package utils

import kapibarabanka.lo3.common.models.domain.{AuthFailed, ConnectionClosed, KindleEmailNotSet, TooManyRequests, Cloudflare}

object ErrorMessage:

  def invalidMessage(messageText: String): String = s"'$messageText' is not parsable AO3 link, don't know what to do :c"
  def invalidQuery(queryText: String): String     = s"Don't know how to answer the query $queryText"

  def fromThrowable(error: Throwable, actionName: String): String =
    error match
      case TooManyRequests()   => rateLimit
      case ConnectionClosed()  => timeout
      case KindleEmailNotSet() => noKindleEmail
      case AuthFailed()        => authFailed
      case Cloudflare()        => shieldsAreUp
      case _                   => defaultMessage(error, actionName)

  private def defaultMessage(error: Throwable, actionName: String) = s"\nError happened while $actionName: ${error.getMessage}"

  private val noKindleEmail: String = "Couldn't find kindle email for this user, please notify the bot's author"

  private val authFailed =
    s"Failed to log in to the Ao3. Please try again in a couple of minutes or check <a href=\"https://x.com/ao3_status\">Ao3 status twitter</a>"

  private val rateLimit =
    """
      |The bot has sent too many requests to the Ao3 and is now being rate-limited. Please try again in a couple of minutes. Parsing progress is saved, so even if you get this message again, after some number of attempts fic will be fully parsed.
      |
      |If you were parsing a series, please try to parse its works one by one and then try parsing the series again.
      |""".stripMargin

  private val timeout =
    """
      |Parsing took to long and the connection to API closed, please resend this fic if you want to try again
      |
      |Parsing progress is saved, so even if you get this message again, after some number of attempts fic will be fully parsed. If you were parsing a series, please try to parse its works one by one and then try parsing the series again.
      |
      |This error will likely persist till April, the reason is described <a href="https://archiveofourown.org/admin_posts/31204">on the Archive itself</a>
      |""".stripMargin

  private val shieldsAreUp =
    """
      |Cloudflare bot checks are up on Ao3, please try again in a couple of minutes
      |
      |This error will likely persist till April, the reason is described <a href="https://archiveofourown.org/admin_posts/31204">on the Archive itself</a>
      |""".stripMargin
