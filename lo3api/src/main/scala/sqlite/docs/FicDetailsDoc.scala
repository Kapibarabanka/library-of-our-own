package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.{FicDetails, UserImpression}

import java.time.LocalDate

case class FicDetailsDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    impression: Option[String],
    fire: Boolean,
    recordCreated: String
):
  def toModel: FicDetails = FicDetails(
    backlog = backlog,
    isOnKindle = isOnKindle,
    impression = impression.map(UserImpression.withName),
    spicy = fire,
    recordCreated = LocalDate.parse(recordCreated)
  )
