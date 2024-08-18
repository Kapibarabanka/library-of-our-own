package com.kapibarabanka.kapibarabot.utils

import com.kapibarabanka.ao3scrapper.models.{Fic, Relationship}
import com.kapibarabanka.kapibarabot.domain.*
import scalaz.Scalaz.ToIdOps

object MessageText {
  def existingFic(record: MyFicRecord): String =
    s"""
       |${info(record.fic)}
       |${record.stats |> displayMyRating}
       |${record.stats |> displayStats}
       |<a href="${Constants.entityBaseUrl}${record.id.get}">View on Airtable</a>
       |""".stripMargin

  def newFic(link: String): String =
    s"""
       |<a href="$link">That's a new one!</a>
       |It's not in the Airtable yet, but it could be.
       |""".stripMargin

  private def info(fic: Fic) =
    s"""<b>${fic.title}</b>
       |<i>${fic.authors.mkString(", ")}</i>
       |
       |${fic.relationships.map(formatShip).mkString("\n")}
       |
       |${f"${fic.words}%,d"} words
       |""".stripMargin

  private def displayStats(stats: MyFicStats) =
    s"""${if (stats.backlog) s"${Emoji.backlog} Is in backlog" else s"${Emoji.cross} Not in backlog"}
       |${if (stats.isOnKindle) s"${Emoji.kindle} Is on Kindle" else s"${Emoji.cross} Not on Kindle"}
       |${if (stats.read) s"${Emoji.read} Already read" else s"${Emoji.cross} Not read"}${readDates(stats)}
       |""".stripMargin

  private def displayMyRating(stats: MyFicStats) =
    (if (stats.fire) s"${Emoji.fire}<b>It has fire!</b>${Emoji.fire}\n" else "")
      + stats.quality.fold("")(q => s"You rated it ${formatQuality(q)}\n")
      + stats.comment.fold("")(c => s"\nYour thoughts on it:\n<i>$c</i>")

  private def formatQuality(quality: Quality.Value) = quality match
    case Quality.Brilliant => s"<b>Brilliant</b> ${Emoji.brilliant}"
    case Quality.Nice      => s"<b>Nice</b> ${Emoji.nice}"
    case Quality.Ok        => s"<b>Ok</b> ${Emoji.ok}"
    case Quality.Meh       => s"<b>Meeeh</b> ${Emoji.meh}"
    case Quality.Never     => s"<b>Never</b> Again ${Emoji.never}"

  private def readDates(stats: MyFicStats) = stats.readDatesList match
    case List() => ""
    case dates  => " on: " + dates.mkString(", ")

  private def formatShip(ship: Relationship) = ship.name.replace("/", "  /  ").replace(" & ", "  &  ")
}
