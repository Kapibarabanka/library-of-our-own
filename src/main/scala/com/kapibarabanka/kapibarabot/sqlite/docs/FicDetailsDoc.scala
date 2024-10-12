package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.kapibarabot.domain.{FicDetails, Quality, UserFicKey}

import java.time.LocalDate

case class FicDetailsDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[String],
    fire: Boolean,
    recordCreated: String
):
  def toModel: FicDetails = FicDetails(
    backlog = backlog,
    isOnKindle = isOnKindle,
    quality = quality.map(Quality.withName),
    fire = fire,
    recordCreated = LocalDate.parse(recordCreated)
  )
