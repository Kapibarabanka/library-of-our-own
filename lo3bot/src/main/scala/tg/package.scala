package kapibarabanka.lo3.bot

import sqlite.services.DbService

package object tg:
  val db = DbService(s"${AppConfig.dbPath}${AppConfig.dbName}")
