package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

sealed abstract class Lo3Error(val msg: String) extends Exception(msg)
object Lo3Error:
  implicit val schema: Schema[Lo3Error] = DeriveSchema.gen

case class DbError(dbMessage: String)        extends Lo3Error(s"Error while working with DB: $dbMessage")
case class KindleEmailNotSet()               extends Lo3Error("Kindle email is not set")

case class NotAo3Link(link: String)          extends Lo3Error(s"Link '$link' is not a parsable Ao3 link")
case class TooManyRequests()                 extends Lo3Error("Too many requests")
case class AuthFailed()                      extends Lo3Error("Auth failed")
case class ParsingError(internalMsg: String, attemptedEntityName: String)
  extends Lo3Error(s"Error while parsing $attemptedEntityName:\n$internalMsg")
case class Cloudflare() extends Lo3Error("Shields are up")
case class RestrictedWork(entityName: String) extends Lo3Error(s"$entityName is restricted")
case class NotFoundOnAo3(entityName: String) extends Lo3Error(s"Couldn't find $entityName on Ao3")
case class SomeAo3Error(internalMsg: String) extends Lo3Error(s"Error while working with Ao3: $internalMsg")

case class EmailError(internalMsg: String)   extends Lo3Error(s"Error while working with email: $internalMsg")
case class HttpError(status: Int, actionName: String) extends Lo3Error(s"Got HTTP status $status while $actionName")

case class DownloadLinkNotFound(workId: String)  extends Lo3Error(s"Couldn't find download link for work $workId")
case class UnspecifiedError(internalMsg: String) extends Lo3Error(internalMsg)
case class ConnectionClosed()                    extends Lo3Error("Connection closed due to timeout")
