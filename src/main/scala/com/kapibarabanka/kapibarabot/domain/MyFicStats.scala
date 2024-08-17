package com.kapibarabanka.kapibarabot.domain

import java.time.LocalDate

case class MyFicStats(
    readOption: Option[Boolean] = None,
    backlogOption: Option[Boolean] = None,
    isOnKindleOption: Option[Boolean] = None,
    readDatesOption: Option[String] = None,
    kindleToDoOption: Option[Boolean] = None,
    qualityOption: Option[Quality.Value] = None,
    commentOption: Option[String] = None
):
  val readDatesList: List[LocalDate] = readDatesOption
    .map(s =>
      s.split(", ")
        .map(d => LocalDate.parse(d))
        .toList
    )
    .getOrElse(List())

  val read: Boolean       = readOption.getOrElse(false)
  val backlog: Boolean    = backlogOption.getOrElse(false)
  val isOnKindle: Boolean = isOnKindleOption.getOrElse(false)
  val kindleToDo: Boolean = kindleToDoOption.getOrElse(false)

  val isReadToday: Boolean = if (readDatesList.isEmpty) false else readDatesList.max == LocalDate.now()
  def withReadToday: MyFicStats = if (isReadToday) this.copy()
  else
    this.copy(
      readOption = Some(true),
      backlogOption = Some(false),
      readDatesOption = Some((LocalDate.now() :: this.readDatesList).mkString(", "))
    )
