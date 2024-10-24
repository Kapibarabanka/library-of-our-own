package kapibarabanka.lo3.api

import sqlite.services.DbService

package object controllers:
  val db: DbService = DbService(s"${AppConfig.dbPath}${AppConfig.dbName}")
