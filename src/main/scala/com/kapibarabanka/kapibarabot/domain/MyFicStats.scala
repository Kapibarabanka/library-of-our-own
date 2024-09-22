package com.kapibarabanka.kapibarabot.domain

case class MyFicStats(
    read: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[Quality.Value],
    fire: Boolean
)
