package com.kapibarabanka.kapibarabot.domain

case class FicDisplayModel(
    id: String,
    title: String,
    authors: List[String],
    fandoms: Set[String],
    characters: Set[String],
    relationships: List[String],
    tags: List[String],
    words: Int,
    comments: List[FicComment],
    stats: MyFicStats
)
