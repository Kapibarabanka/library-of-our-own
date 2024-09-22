package com.kapibarabanka.kapibarabot.utils

import com.kapibarabanka.kapibarabot.domain.*
import scalaz.Scalaz.ToIdOps

object MessageText {
  def existingFic(model: FicDisplayModel): String =
    s"""
       |${info(model)}
       |${model |> displayMyRating}
       |${model |> displayStats}
       |""".stripMargin

  def newFic(link: String): String =
    s"""
       |<a href="$link">That's a new one!</a>
       |It's not in the database yet, but it could be.
       |""".stripMargin

  private def info(fic: FicDisplayModel) =
    s"""<b>${fic.title}</b>
       |<i>${fic.authors.mkString(", ")}</i>
       |
       |${fic.relationships.map(formatShip).mkString("\n")}
       |
       |${f"${fic.words}%,d"} words
       |""".stripMargin

  private def displayStats(fic: FicDisplayModel) =
    s"""${if (fic.stats.backlog) s"${Emoji.backlog} Is in backlog" else s"${Emoji.cross} Not in backlog"}
       |${if (fic.stats.isOnKindle) s"${Emoji.kindle} Is on Kindle" else s"${Emoji.cross} Not on Kindle"}
       |${if (fic.stats.read) s"${Emoji.finish} Already read" else s"${Emoji.cross} Not read"}${readDates(fic.readDates)}
       |""".stripMargin

  private def displayMyRating(fic: FicDisplayModel) =
    (if (fic.stats.fire) s"${Emoji.fire}<b>It has fire!</b>${Emoji.fire}\n" else "")
      + fic.stats.quality.fold("")(q => s"You rated it ${formatQuality(q)}\n")
      + (if (fic.comments.isEmpty) ""
         else
           s"\nYour thoughts on it:\n<i>${fic.comments.map(_.format()).mkString("\n")}</i>")

  private def formatQuality(quality: Quality.Value) = quality match
    case Quality.Brilliant => s"<b>Brilliant</b> ${Emoji.brilliant}"
    case Quality.Nice      => s"<b>Nice</b> ${Emoji.nice}"
    case Quality.Ok        => s"<b>Ok</b> ${Emoji.ok}"
    case Quality.Meh       => s"<b>Meeeh</b> ${Emoji.meh}"
    case Quality.Never     => s"<b>Never</b> Again ${Emoji.never}"

  private def readDates(dates: List[ReadDates]) = dates match
    case List() => ""
    case dates =>
      ":\n" + dates
        .map(d => s"from ${d.startDate.getOrElse(d.finishDate.getOrElse("..."))} to ${d.finishDate.getOrElse("...")}")
        .sorted
        .mkString("\n")

  private def formatShip(shipName: String) = shipName.replace("/", "  /  ").replace(" & ", "  &  ")
}
