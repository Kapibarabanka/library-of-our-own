package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}

case class UserFicKey(userId: String, ficId: String, ficType: FicType):
  val ficIsSeries: Boolean = ficType == FicType.Series

object UserFicKey:
  def fromBool(userId: String, ficId: String, isSeries: Boolean): UserFicKey =
    UserFicKey(userId, ficId, if (isSeries) FicType.Series else FicType.Work)
