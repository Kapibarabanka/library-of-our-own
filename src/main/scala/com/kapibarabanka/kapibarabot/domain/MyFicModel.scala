package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{Character, Fandom, Relationship}

case class MyFicModel(
    id: String,
    title: String,
    fandoms: List[Fandom],
    characters: List[Character],
    relationships: List[Relationship],
    tags: List[String]
)
