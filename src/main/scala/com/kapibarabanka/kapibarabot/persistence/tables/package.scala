package com.kapibarabanka.kapibarabot.persistence

import com.kapibarabanka.kapibarabot.persistence.docs.*
import io.circe.Decoder
import io.circe.derivation.{Configuration, ConfiguredDecoder}

package object tables {
  implicit val config: Configuration = Configuration.default.copy(
    useDefaults = true
  )

  implicit val tagDecoder: Decoder[TagDocument]           = ConfiguredDecoder.derived[TagDocument]
  implicit val statsDecoder: Decoder[StatsDocument]       = ConfiguredDecoder.derived[StatsDocument]
  implicit val ficDecoder: Decoder[FicDocument]           = ConfiguredDecoder.derived[FicDocument]
  implicit val shipDecoder: Decoder[RelationshipDocument] = ConfiguredDecoder.derived[RelationshipDocument]
}
