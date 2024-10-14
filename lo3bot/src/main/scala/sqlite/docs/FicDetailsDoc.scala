package kapibarabanka.lo3.bot
package sqlite.docs

import kapibarabanka.lo3.models.tg.{FicDetails, Quality, UserFicKey}

import java.time.LocalDate

case class FicDetailsDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[String],
    fire: Boolean,
    recordCreated: String
):
  def toModel: FicDetails = FicDetails(
    backlog = backlog,
    isOnKindle = isOnKindle,
    quality = quality.map(Quality.withName),
    fire = fire,
    recordCreated = LocalDate.parse(recordCreated)
  )
