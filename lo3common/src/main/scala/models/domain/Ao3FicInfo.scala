package kapibarabanka.lo3.common
package models.domain

import models.ao3.{FicType, Rating}

import zio.schema.{DeriveSchema, Schema}

case class Ao3FicInfo(
    id: String,
    link: String,
    ficType: FicType,
    title: String,
    authors: List[String] = List(),
    rating: Rating.Value,
    categories: Set[String] = Set(),
    warnings: Set[String] = Set(),
    fandoms: Set[String] = Set(),
    characters: Set[String] = Set(),
    relationships: List[String] = List(),
    tags: List[String] = List(),
    words: Int,
    complete: Boolean,
    partsWritten: Int
)

object Ao3FicInfo:
  implicit val schema: Schema[Ao3FicInfo] = DeriveSchema.gen
