package com.kapibarabanka.kapibarabot.persistence

import com.kapibarabanka.kapibarabot.persistence.docs.*
import io.circe.Decoder
import io.circe.derivation.{Configuration, ConfiguredDecoder}

package object tables {
  implicit val config: Configuration = Configuration.default.copy(
    useDefaults = true
  )
  implicit val ficDisplauDecoder: Decoder[FicDisplayDoc]  = ConfiguredDecoder.derived[FicDisplayDoc]
}
