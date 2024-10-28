package kapibarabanka.lo3.common
package openapi

import zio.schema.{DeriveSchema, Schema}

case class NotFoundError()
object NotFoundError:
  implicit val schema: Schema[NotFoundError] = DeriveSchema.gen
