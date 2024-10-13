package kapibarabanka.lo3.bot
package domain

object Quality extends Enumeration {
  type Quality = Value
  val Never     = Value("Never again")
  val Meh       = Value("Meh")
  val Ok        = Value("Ok")
  val Nice      = Value("Nice")
  val Brilliant = Value("Brilliant")
}
