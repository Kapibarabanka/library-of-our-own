package kapibarabanka.lo3.common
package services

import scalaz.Scalaz.ToIdOps
import zio.*

import java.io.File
import java.time.LocalDateTime
import scala.util.Try

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

  def parseDateTime(str: String, id: Option[Int]) =
    Try(LocalDateTime.parse(str)).getOrElse(LocalDateTime.parse(str + s"T00:00:00${id.map(i => s".$i").getOrElse("")}"))
