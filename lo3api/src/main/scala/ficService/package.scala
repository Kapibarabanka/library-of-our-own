package kapibarabanka.lo3.api

import sqlite.services.Lo3DataService

import kapibarabanka.lo3.common.AppConfig

package object ficService:
  val data: Lo3DataService = Lo3DataService(s"${AppConfig.dbPath}${AppConfig.dbName}")
