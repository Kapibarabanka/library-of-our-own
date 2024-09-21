package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{FicType, Rating}

case class FicDisplayModel(
    id: String,
    link: String,
    ficType: FicType,
    title: String,
    authors: List[String],
    rating: Rating.Value,
    fandoms: Set[String],
    characters: Set[String],
    relationships: List[String],
    tags: List[String],
    words: Int,
    complete: Boolean,
    comments: List[FicComment],
    stats: MyFicStats
)
