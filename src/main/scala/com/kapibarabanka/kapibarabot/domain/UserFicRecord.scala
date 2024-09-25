package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{FicType, Rating}

case class UserFicRecord(
    userId: String,
    fic: FlatFicModel,
    readDatesInfo: ReadDatesInfo,
    comments: List[FicComment],
    details: FicDetails
):
  val key: UserFicKey = UserFicKey(userId, fic.id, fic.ficType)
