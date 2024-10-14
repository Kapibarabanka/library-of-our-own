package kapibarabanka.lo3.models
package tg

import java.time.LocalDate

case class FicDetails(
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[Quality.Value],
    fire: Boolean,
    recordCreated: LocalDate
)
