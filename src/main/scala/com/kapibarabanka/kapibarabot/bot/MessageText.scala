package com.kapibarabanka.kapibarabot.bot

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
       |${fic.words} words
       |""".stripMargin

  private def displayStats(stats: MyFicStats) =
    s"""${if (stats.backlogOption.getOrElse(false)) s"${Emoji.backlog} Is in backlog" else s"${Emoji.cross} Not in backlog"}
       |${if (stats.isOnKindleOption.getOrElse(false)) s"${Emoji.kindle} Is on Kindle" else s"${Emoji.cross} Not on Kindle"}
       |${if (stats.readOption.getOrElse(false)) s"${Emoji.read} Already read" else s"${Emoji.cross} Not read"}${readDates(stats)}
       |""".stripMargin

  private def displayMyRating(stats: MyFicStats) =
    stats.qualityOption.fold("")(q => s"<u>You rated it</u> ${formatQuality(q)}\n") + stats.commentOption.fold("")(c =>
      s"<u>\nYour thoughts on it:\n</u><i>$c</i>"
    )

  private def formatQuality(quality: Quality.Value) = quality match
    case Quality.Brilliant => s"Brilliant ${Emoji.brilliant}"
    case Quality.Nice      => s"Nice ${Emoji.nice}"
    case Quality.Ok        => s"Ok ${Emoji.ok}"
    case Quality.Meh       => s"Meeeh ${Emoji.meh}"
    case Quality.Never     => s"Never Again ${Emoji.never}"

  private def readDates(stats: MyFicStats) = stats.readDatesList match
    case List() => ""
    case dates  => " on: " + dates.mkString(", ")

  private def formatShip(ship: Relationship) = ship.name.replace("/", "  /  ").replace(" & ", "  &  ")
}
