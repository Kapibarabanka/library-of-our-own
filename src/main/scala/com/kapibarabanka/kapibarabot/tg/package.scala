package com.kapibarabanka.kapibarabot

import com.kapibarabanka.kapibarabot.sqlite.services.DbService

package object tg:
  val db = DbService(s"${AppConfig.dbPath}${AppConfig.dbName}")
