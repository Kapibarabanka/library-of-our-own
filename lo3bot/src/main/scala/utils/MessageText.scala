package kapibarabanka.lo3.bot
package utils

import kapibarabanka.lo3.common.models.domain.*
import kapibarabanka.lo3.common.models.tg.*
import scalaz.Scalaz.ToIdOps

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MessageText {
  def newFic(link: String): String =
    s"""
       |<a href="$link">That's a new one!</a>
       |It's not in the database yet, but it could be.
       |Please note that parsing the AO3 can take some time.
       |""".stripMargin

  def existingFic(record: UserFicRecord): String =
    s"""
       |${info(record.fic, record.key.ficIsSeries)}
       |${record |> displayMyRating}
       |${record |> displayStats}
       |""".stripMargin

  val help: String =
    """
      |Send me a link to a fic (it can be a single work or a series) on Ao3. If it is not in my database yet, I will parse its info (title, author, ships, tags, word count etc.) from AO3. After that you can mark and track when you started/finished reading that fic, rate it and leave notes (visible only to you).
      |
      |If you provide your Kindle email with /setKindleEmail command you can send a fic to your Kindle library. If you send a series to Kindle, I will compose all its works into a single document
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

  private def info(fic: FlatFicModel, isSeries: Boolean) =
    val header = s"""<b>${fic.title}</b>
                    |<i>${fic.authors.mkString(", ")}</i>
                    |
                    |${fic.relationships.map(formatShip).mkString("\n")}""".stripMargin
    val tags   = fic.tags.mkString(",   ")
    val s      = if (fic.partsWritten.toString.last == '1' && fic.partsWritten != 11) "" else "s"
    val footer = s"${f"${fic.partsWritten}%,d"} ${if (isSeries) "work" else "chapter"}$s, ${f"${fic.words}%,d"} words"
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
         |$footer
         ||""".stripMargin

  private def displayStats(record: UserFicRecord) =
    s"""${if (record.details.backlog) s"${Emoji.backlog} Is in backlog" else s"${Emoji.cross} Not in backlog"}
       |${if (record.details.isOnKindle) s"${Emoji.kindle} Is on Kindle" else s"${Emoji.cross} Not on Kindle"}
       |${readDates(record)}
       |""".stripMargin

  private def displayMyRating(record: UserFicRecord) =
    (if (record.details.fire) s"${Emoji.fire}<b>It has fire!</b>${Emoji.fire}\n" else "")
      + record.details.quality.fold("")(q => s"You rated it ${formatQuality(q)}\n")
      + (if (record.comments.isEmpty) ""
         else
           s"\nYour thoughts on it:\n<i>${record.comments.map(_.format()).mkString("\n")}</i>")

  private def formatQuality(quality: Quality.Value) = quality match
    case Quality.Brilliant => s"<b>Brilliant</b> ${Emoji.brilliant}"
    case Quality.Nice      => s"<b>Nice</b> ${Emoji.nice}"
    case Quality.Ok        => s"<b>Ok</b> ${Emoji.ok}"
    case Quality.Meh       => s"<b>Meeeh</b> ${Emoji.meh}"
    case Quality.Never     => s"<b>Never</b> Again ${Emoji.never}"

  private def readDates(record: UserFicRecord) =
    (if (record.readDatesInfo.finishedReading)
       s"${Emoji.finish} Already read\n"
     else
       s"${Emoji.cross} Not read\n")
      + record.readDatesInfo.readDates
        .map {
          case StartAndFinish(start, finish) if start == finish => s"   - on ${format(start)} (read in one day)"
          case StartAndFinish(start, finish)                    => s"   - from ${format(start)} to ${format(finish)}"
          case Start(date)                                      => s"   - started reading on ${format(date)}"
          case SingleDayRead(date)                              => s"   - on ${format(date)} (read in one day)"
        }
        .mkString("\n")

  private def format(isoDate: String) = LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("dd MMM uuuu"))

  private def formatShip(shipName: String) =
    shipName.replace("/", s"  ${Emoji.romantic}  ").replace(" & ", s"  ${Emoji.platonic}  ")
}
