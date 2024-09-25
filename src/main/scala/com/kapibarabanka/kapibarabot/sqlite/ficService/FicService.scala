package com.kapibarabanka.kapibarabot.sqlite.ficService

import zio.IO

trait FicService:
  def initDb: IO[Throwable, Unit]
