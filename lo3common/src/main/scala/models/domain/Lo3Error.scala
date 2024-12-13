package kapibarabanka.lo3.common
package models.domain

import models.ao3
import models.ao3.*

import zio.schema.{DeriveSchema, Schema}

sealed abstract class Lo3Error(val msg: String) extends Exception(msg)
object Lo3Error:
  implicit val schema: Schema[Lo3Error] = DeriveSchema.gen
  def fromAo3Error(error: Ao3Error) = error match
    case ao3.NotAo3Link(link)  => NotAo3Link(link)
    case ao3.TooManyRequests() => TooManyRequests()
    case ao3.AuthFailed()      => AuthFailed()
    case e                     => SomeAo3Error(e.msg)

case class DbError(dbMessage: String)            extends Lo3Error(s"Error while working with DB: $dbMessage")
case class KindleEmailNotSet()                   extends Lo3Error("Kindle email is not set")
case class NotAo3Link(link: String)              extends Lo3Error(s"Link '$link' is not a parsable Ao3 link")
case class TooManyRequests()                     extends Lo3Error("Too many requests")
case class AuthFailed()                          extends Lo3Error("Auth failed")
case class SomeAo3Error(internalMsg: String)     extends Lo3Error(s"Error while working with Ao3: $internalMsg")
case class EmailError(internalMsg: String)       extends Lo3Error(s"Error while working with email: $internalMsg")
case class UnspecifiedError(internalMsg: String) extends Lo3Error(internalMsg)