package kapibarabanka.lo3.bot
package utils

import kapibarabanka.lo3.common.models.domain.*
import scalaz.Scalaz.ToIdOps

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MessageText {
  def existingFic(fic: Fic): String =
    s"""
       |${info(fic.ao3Info, fic.key.ficIsSeries)}
       |${fic |> displayMyRating}
       |${fic |> displayStats}
       |""".stripMargin

  val help: String =
    """
      |Send me a link to a fic (it can be a single work or a series) on Ao3. If it is not in my database yet, I will parse its info (title, author, ships, tags, word count etc.) from AO3. After that you can mark and track when you started/finished reading that fic, rate it and leave notes (visible only to you).
      |
      |If you provide your Kindle email with /set_email command you can send a fic to your Kindle library. If you send a series to Kindle, I will compose all its works into a single document
      |
      |You can also add fics to the backlog and get a filterable HTML file with full backlog with /backlog command
      |""".stripMargin

  val kindleSteps =
    """
      |<b>Step 1</b>
      |Add ao3bot@gmx.com to your <i>Approved Personal Document E-mail List</i> (<a href="https://www.amazon.com/hz/mycd/myx#/home/settings/payment">Preferences</a> -> Personal Document Settings)
      |
      |<b>Step 2</b>
      |In the same Preferences section, enable <i>Personal Document Archiving</i> so that your reading progress and bookmarks were saved to your amazon account and could be synced between devices.
      |
      |<b>Step 3</b>
      |Send me your Kindle email address. To find your Kindle email address, visit the <a href="https://www.amazon.com/mn/dcw/myx.html#/home/devices/1">Manage your Devices page</a>. Then select your primary reading device (it can be Kindle or a Kindle app on your phone/tablet) and copy its email.
      |
      |Example: myemail@kindle.com
      |""".stripMargin

  private def info(ao3Info: Ao3FicInfo, isSeries: Boolean) =
    val header = s"""<b>${ao3Info.title}</b>
                    |<i>${ao3Info.authors.mkString(", ")}</i>
                    |
                    |${ao3Info.relationships.map(formatShip).mkString("\n")}""".stripMargin
    val tags   = ao3Info.tags.mkString(",   ")
    val s      = if (ao3Info.partsWritten.toString.last == '1' && ao3Info.partsWritten != 11) "" else "s"
    val footer = s"${f"${ao3Info.partsWritten}%,d"} ${if (isSeries) "work" else "chapter"}$s, ${f"${ao3Info.words}%,d"} words"
    val withTags =
      s"""$header
         |
         |$tags
         |
         |$footer
         ||""".stripMargin
    if (withTags.length <= 3500)
      withTags
    else
      s"""$header
         |
         |<i>Tags don't fit into Telegram character limit</i>
         |
         |$footer
         ||""".stripMargin

  private def displayStats(fic: Fic) =
    s"""${if (fic.details.backlog) s"${Emoji.backlog} Is in backlog" else s"${Emoji.cross} Not in backlog"}
       |${if (fic.details.isOnKindle) s"${Emoji.kindle} Is on Kindle" else s"${Emoji.cross} Not on Kindle"}
       |${readDates(fic)}
       |""".stripMargin

  private def displayMyRating(fic: Fic) =
    (if (fic.details.spicy) s"${Emoji.fire}<b>It's spicy!</b>${Emoji.fire}\n" else "")
      + fic.details.impression.fold("")(q => s"Your impression is ${formatImpresson(q)}\n")
      + (if (fic.notes.isEmpty) ""
         else
           s"\nYour thoughts on it:\n<i>${fic.notes.map(_.format()).mkString("\n\n")}</i>\n")

  private def formatImpresson(impression: UserImpression.Value) = impression match
    case UserImpression.Brilliant => s"<b>Brilliant</b> ${Emoji.brilliant}"
    case UserImpression.Nice      => s"<b>Nice</b> ${Emoji.nice}"
    case UserImpression.Ok        => s"<b>Ok</b> ${Emoji.ok}"
    case UserImpression.Meh       => s"<b>Meeeh</b> ${Emoji.meh}"
    case UserImpression.Never     => s"<b>Never</b> Again ${Emoji.never}"

  private def readDates(fic: Fic) =
    (if (fic.readDatesInfo.alreadyRead)
       s"${Emoji.finish} Already read\n"
     else
       s"${Emoji.cross} Not read\n")
      + fic.readDatesInfo.readDates
        .map {
          case ReadDates(_, start, Some(finish), false) if start == finish => s"   - on ${format(start)} (read in one day)"
          case ReadDates(_, start, Some(finish), false)                    => s"   - from ${format(start)} to ${format(finish)}"
          case ReadDates(_, start, None, _)                                => s"   - started reading on ${format(start)}"
          case ReadDates(_, start, Some(finish), true) => s"   - from ${format(start)} to ${format(finish)} (abandoned)"
        }
        .mkString("\n")

  private def format(date: LocalDateTime) = date.format(DateTimeFormatter.ofPattern("dd MMM uuuu"))

  private def formatShip(shipName: String) =
    shipName.replace("/", s"  ${Emoji.romantic}  ").replace(" & ", s"  ${Emoji.platonic}  ")
}
