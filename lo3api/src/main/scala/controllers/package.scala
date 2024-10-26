package kapibarabanka.lo3.api

import sqlite.services.Lo3DataService

package object controllers:
  val data: Lo3DataService = Lo3DataService(s"${AppConfig.dbPath}${AppConfig.dbName}")
