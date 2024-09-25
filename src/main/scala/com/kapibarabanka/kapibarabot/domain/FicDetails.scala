package com.kapibarabanka.kapibarabot.domain

import java.time.LocalDate

case class FicDetails(
    read: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[Quality.Value],
    fire: Boolean,
    recordCreated: LocalDate
)
