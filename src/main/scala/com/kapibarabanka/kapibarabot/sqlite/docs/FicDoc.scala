package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.{Character, Fandom, Relationship}
import com.kapibarabanka.kapibarabot.domain.MyFicModel

case class FicDoc(id: String, title: String):
  def toModel(fandoms: Seq[Fandom], characters: Seq[Character], relationships: Seq[Relationship], tags: Seq[String]): MyFicModel =
    MyFicModel(
      id = id,
      title = title,
      fandoms = fandoms.toSet,
      tags = tags.toList,
      characters = characters.toSet,
      relationships = relationships.toList
    )

object FicDoc:
  def fromModel(fic: MyFicModel): FicDoc = FicDoc(id = fic.id, title = fic.title)
