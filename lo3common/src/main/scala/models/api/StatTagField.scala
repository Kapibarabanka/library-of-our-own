package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

enum StatTagField:
  case Ship, Fandom, Tag
  
object StatTagField:
  implicit val schema: Schema[StatTagField] = DeriveSchema.gen