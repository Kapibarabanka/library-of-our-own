package com.kapibarabanka.kapibarabot.sqlite

import zio.Task

trait WithDb(userId: String):
  protected def db[T] = Sqlite.run[T](userId)
  def initIfNotExists: Task[Unit]
