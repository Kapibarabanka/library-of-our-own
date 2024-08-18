package com.kapibarabanka.kapibarabot.domain

import java.time.LocalDate

case class MyFicStats(
    read: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    readDates: Option[String],
    kindleToDo: Boolean,
    quality: Option[Quality.Value],
    fire: Boolean,
    comment: Option[String]
):
  val readDatesList: List[LocalDate] = readDates
    .map(s =>
      s.split(", ")
        .map(d => LocalDate.parse(d))
        .toList
    )
    .getOrElse(List())

  val isReadToday: Boolean = if (readDatesList.isEmpty) false else readDatesList.max == LocalDate.now()

  def withReadToday: MyFicStats = if (isReadToday) this.copy()
  else
    this.copy(
      read = true,
      backlog = false,
      readDates = Some((LocalDate.now() :: this.readDatesList).mkString(", "))
    )
