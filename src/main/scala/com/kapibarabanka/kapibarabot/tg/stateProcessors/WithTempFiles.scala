package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.AppConfig
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import zio.*

import java.io.File

trait WithTempFiles(bot: BotWithChatId) extends WithErrorHandling:
  def useTempFile(fileName: String)(action: File => Task[Unit]): UIO[Unit] = {
    def acquire = for {
      file <- ZIO.attempt(File(AppConfig.tempDir + fileName))
      _ <- ZIO.log(s"Created file ${file.getPath}")
    } yield file

    def deleteFile(temp: File) = for {
      path <- ZIO.succeed(temp.getPath)
      _ <- ZIO.succeed(temp.delete())
      _ <- ZIO.log(s"Deleted file $path")
    } yield ()

    def use(temp: File) = for {
//      _ <- ZIO.log(s"Downloading file form $url")
//      _ <- ZIO.attempt(new URL(url) #> temp !!) |> sendOnError({})(s"downloading file from $url")
//      _ <- ZIO.log(s"Finished download form $url")
      _ <- action(temp)
    } yield ()

    ZIO.acquireReleaseWith(acquire)(deleteFile)(action) |> sendOnError({})("working with temp file")
  }
  
