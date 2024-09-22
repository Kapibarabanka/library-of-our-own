package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}

case class FicKey(ficId: String, ficType: FicType):
  val isSeries: Boolean = ficType == FicType.Series

object FicKey:
  def fromBool(ficId: String, isSeries: Boolean): FicKey = FicKey(ficId, if (isSeries) FicType.Series else FicType.Work)
  def fromWork(work: Work): FicKey                       = FicKey(work.id, FicType.Work)
  def fromSeries(series: Series): FicKey                 = FicKey(series.id, FicType.Series)
