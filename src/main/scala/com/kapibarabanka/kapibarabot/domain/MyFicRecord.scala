package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.Fic

case class MyFicRecord(fic: Fic, id: Option[String] = None, stats: MyFicStats)