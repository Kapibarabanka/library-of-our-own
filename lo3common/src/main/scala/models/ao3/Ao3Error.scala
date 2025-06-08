package kapibarabanka.lo3.common.models.ao3

import zio.schema.{DeriveSchema, Schema}

sealed abstract class Ao3Error(val msg: String) extends Exception(msg)
object Ao3Error:
  implicit val exSchena: Schema[Ao3Error] = DeriveSchema.gen

case class AuthFailed() extends Ao3Error("Failed to authenticate")

case class TooManyRequests() extends Ao3Error("HTTP status 429: Too many requests")

case class NotFound(entityName: String) extends Ao3Error(s"Couldn't find $entityName on Ao3")

case class RestrictedWork(entityName: String) extends Ao3Error(s"$entityName is restricted")

case class DownloadLinkNotFound(workId: String) extends Ao3Error(s"Couldn't find download link for work $workId")

case class HttpError(status: Int, actionName: String) extends Ao3Error(s"Got HTTP status $status while $actionName")

case class ParsingError(internalMsg: String, attemptedEntityName: String)
    extends Ao3Error(s"Error while parsing $attemptedEntityName:\n$internalMsg")

case class NotAo3Link(link: String) extends Ao3Error(s"Link '$link' is not a parsable Ao3 link")

case class Cloudflare() extends Ao3Error("Shields are up")

case class UnspecifiedError(message: String) extends Ao3Error(message)
