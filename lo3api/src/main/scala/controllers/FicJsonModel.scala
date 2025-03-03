package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.Fic
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class FicJsonModel(
    id: String,
    isSeries: Boolean,
    link: String,
    title: String,
    authors: List[String],
    rating: String,
    categories: Set[String],
    warnings: Set[String],
    fandoms: Set[String],
    characters: Set[String],
    relationships: List[String],
    tags: List[String],
    words: Int,
    complete: Boolean
)

object FicJsonModel:
  implicit val encoder: JsonEncoder[FicJsonModel] = DeriveJsonEncoder.gen[FicJsonModel]
  def fromRecord(record: Fic): FicJsonModel = FicJsonModel(
    id = record.ao3Info.id,
    isSeries = record.ao3Info.ficType == FicType.Series,
    link = record.ao3Info.link,
    title = record.ao3Info.title,
    authors = record.ao3Info.authors,
    rating = record.ao3Info.rating.toString,
    categories = record.ao3Info.categories,
    warnings = record.ao3Info.warnings,
    fandoms = record.ao3Info.fandoms,
    characters = record.ao3Info.characters,
    relationships = record.ao3Info.relationships,
    tags = record.ao3Info.tags ++ record.specialTags,
    words = record.ao3Info.words,
    complete = record.ao3Info.complete
  )
