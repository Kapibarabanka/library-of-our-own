package kapibarabanka.lo3.models
package ao3

object Rating extends Enumeration {
  type Rating = Value
  val None = Value("Not Rated")
  val General = Value("General Audiences")
  val Teen = Value("Teen And Up Audiences")
  val Mature = Value("Mature")
  val Explicit = Value("Explicit")
}