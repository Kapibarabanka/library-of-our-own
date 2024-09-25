package com.kapibarabanka.kapibarabot.airtable

import com.kapibarabanka.kapibarabot.airtable.docs.*
import io.circe.Decoder
import io.circe.derivation.{Configuration, ConfiguredDecoder}

package object tables {
  implicit val config: Configuration = Configuration.default.copy(
    useDefaults = true
  )
  implicit val ficDisplauDecoder: Decoder[FicDisplayDoc]  = ConfiguredDecoder.derived[FicDisplayDoc]
}
