package com.kapibarabanka.kapibarabot.domain

case class FlatFic(
  id: String,
  title: String,
  fandoms: List[String],
  characters: Set[String],
  relationships: List[String],
  tags: List[String]
)

