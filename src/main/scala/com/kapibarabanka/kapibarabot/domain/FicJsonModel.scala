package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.domain.FicType
import zio.json.*

case class FicJsonModel(
    id: String,
    isSeries: Boolean,
    link: String,
    title: String,
    authors: List[String],
    rating: Char,
    categories: Set[String],
    fandoms: Set[String],
    characters: Set[String],
    relationships: List[String],
    tags: List[String],
    words: Int,
    complete: Boolean
)

object FicJsonModel:
  implicit val encoder: JsonEncoder[FicJsonModel] = DeriveJsonEncoder.gen[FicJsonModel]
  def fromDisplayModel(fic: FlatFicModel): FicJsonModel = FicJsonModel(
    id = fic.id,
    isSeries = fic.ficType == FicType.Series,
    link = fic.link,
    title = fic.title,
    authors = fic.authors,
    rating = fic.rating.toString.head,
    categories = fic.categories,
    fandoms = fic.fandoms,
    characters = fic.characters,
    relationships = fic.relationships,
    tags = fic.tags,
    words = fic.words,
    complete = fic.complete
  )
