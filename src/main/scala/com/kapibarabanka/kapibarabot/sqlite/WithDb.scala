package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import zio.Task

trait WithDb(userId: String):
  protected def db[T] = Sqlite.run[T](userId)
