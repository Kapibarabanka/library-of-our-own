package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.{Fandom, Character, Relationship}
import com.kapibarabanka.kapibarabot.domain.MyFicModel

case class FicDoc(id: String, title: String):
  def toModel(fandoms: Seq[Fandom], characters: Seq[Character], relationships: Seq[Relationship], tags: Seq[String]): MyFicModel =
    MyFicModel(
      id = id,
      title = title,
      fandoms = fandoms.toList,
      tags = tags.toList,
      characters = characters.toList,
      relationships = relationships.toList
    )

object FicDoc:
  def fromModel(fic: MyFicModel): FicDoc = FicDoc(id = fic.id, title = fic.title)
