package kapibarabanka.lo3.bot
package tg.models

import kapibarabanka.lo3.models.ao3.FicType
import kapibarabanka.lo3.models.tg.UserFicRecord
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class FicJsonModel(
    id: String,
    isSeries: Boolean,
    link: String,
    title: String,
    authors: List[String],
    rating: String,
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
  def fromRecord(record: UserFicRecord): FicJsonModel = FicJsonModel(
    id = record.fic.id,
    isSeries = record.fic.ficType == FicType.Series,
    link = record.fic.link,
    title = record.fic.title,
    authors = record.fic.authors,
    rating = record.fic.rating.toString,
    categories = record.fic.categories,
    fandoms = record.fic.fandoms,
    characters = record.fic.characters,
    relationships = record.fic.relationships,
    tags = record.fic.tags ++ record.specialTags,
    words = record.fic.words,
    complete = record.fic.complete
  )