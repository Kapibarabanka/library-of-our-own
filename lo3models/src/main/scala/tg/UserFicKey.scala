package kapibarabanka.lo3.models
package tg

import ao3.FicType

import zio.schema.{DeriveSchema, Schema}

case class UserFicKey(userId: String, ficId: String, ficType: FicType):
  val ficIsSeries: Boolean = ficType == FicType.Series

object UserFicKey:
  def fromBool(userId: String, ficId: String, isSeries: Boolean): UserFicKey =
    UserFicKey(userId, ficId, if (isSeries) FicType.Series else FicType.Work)
  implicit val schema: Schema[UserFicKey] = DeriveSchema.gen
