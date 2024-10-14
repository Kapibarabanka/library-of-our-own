package kapibarabanka.lo3.models
package tg

import ao3.{FicType, Rating}

case class FlatFicModel(
    id: String,
    link: String,
    ficType: FicType,
    title: String,
    authors: List[String],
    rating: Rating.Value,
    categories: Set[String],
    fandoms: Set[String],
    characters: Set[String],
    relationships: List[String],
    tags: List[String],
    words: Int,
    complete: Boolean
)
