package kapibarabanka.lo3.models
package ao3

object Category extends Enumeration {
  type Category = Value
  val FF    = Value("F/F")
  val FM    = Value("F/M")
  val Gen   = Value("Gen")
  val MM    = Value("M/M")
  val Multi = Value("Multi")
  val Other = Value("Other")
  val None  = Value("No category")
}
