package kapibarabanka.lo3.models
package ao3

import zio.schema.{DeriveSchema, Schema}

enum FicType:
  case Work, Series

object FicType:
  implicit val schema: Schema[FicType] = DeriveSchema.gen
