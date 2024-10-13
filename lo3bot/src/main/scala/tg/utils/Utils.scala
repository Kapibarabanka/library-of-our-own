package kapibarabanka.lo3.bot
package tg.utils

import tg.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import zio.*

import java.io.File

object Utils:
  def useTempFile(fileName: String)(action: File => Task[Unit]): Task[Unit] = {
    def acquire = for {
      file <- ZIO.attempt(File(AppConfig.tempDir + fileName))
      _    <- ZIO.log(s"Created file ${file.getPath}")
    } yield file

    def deleteFile(temp: File) = for {
      path <- ZIO.succeed(temp.getPath)
      _    <- ZIO.succeed(temp.delete())
      _    <- ZIO.log(s"Deleted file $path")
    } yield ()

    ZIO.acquireReleaseWith(acquire)(deleteFile)(action)
  }
