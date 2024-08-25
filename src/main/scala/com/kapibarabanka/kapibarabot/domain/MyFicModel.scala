package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.Character

case class MyFicModel(id: String, title: String, tags: List[String], characters: List[Character])

