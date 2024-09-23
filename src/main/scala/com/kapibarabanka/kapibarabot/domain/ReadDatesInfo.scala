package com.kapibarabanka.kapibarabot.domain

case class ReadDatesInfo(
    readDates: List[ReadDates],
    canAddStart: Boolean,
    canAddFinish: Boolean,
    canCancelStart: Boolean,
    canCancelFinish: Boolean
)
