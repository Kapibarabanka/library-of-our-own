package com.kapibarabanka.kapibarabot.domain

object Quality extends Enumeration {
  type Quality = Value
  val Brilliant = Value("Brilliant")
  val Nice      = Value("Nice")
  val Ok        = Value("Ok")
  val Meh       = Value("Meh")
  val Never     = Value("NeverAgain")
}
