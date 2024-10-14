package kapibarabanka.lo3.bot
package tg.utils

import kapibarabanka.lo3.models.tg.*
import tg.utils.Emoji
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
       |${info(record.fic)}
       |${record |> displayMyRating}
       |${record |> displayStats}
       |""".stripMargin

  val help: String =
    """
      |Send me a link to a work or series on Ao3. If it is not in my fic database (shared between all users) you can ask me to parse and save it.
      |
      |After that you can mark and track when you started/finished reading that fic, rate it, leave comments (visible only to you), and mark if the fic has fire (he-he)
      |
      |If you provide your Kindle email with /setKindleEmail command you can send a work to your Kindle library. This feature currently doesn't work for series so please send them one work at a time.
      |
      |You can also add fics to the backlog and get a filterable HTML file with full backlog with /backlog command
      |""".stripMargin

  private def info(fic: FlatFicModel) =
    s"""<b>${fic.title}</b>
       |<i>${fic.authors.mkString(", ")}</i>
       |
       |${fic.relationships.map(formatShip).mkString("\n")}
       |
       |${fic.tags.mkString(",   ")}
       |
       |${f"${fic.words}%,d"} words
       |""".stripMargin

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
