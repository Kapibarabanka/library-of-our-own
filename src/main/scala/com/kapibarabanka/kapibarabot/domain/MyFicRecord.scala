package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{Fic, Series, Work}

case class MyFicRecord(fic: Fic, id: Option[String] = None, stats: MyFicStats):
  def toDisplayModel(): FicDisplayModel = FicDisplayModel(
    id = fic.id,
    isSeries = fic match
      case Series => true
      case _  => false,
    title = fic.title,
    authors = fic.authors,
    fandoms = fic.fandoms.map(f => f.name),
    characters = fic.characters.map(c => c.name),
    relationships = fic.relationships.map(_.name),
    tags = fic.freeformTags.map(_.name),
    words = fic.words.toInt,
    comments = List(),
    stats = stats
  )
