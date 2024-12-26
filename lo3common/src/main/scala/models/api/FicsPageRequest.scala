package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class FicsPageRequest (userId: String, pageSize: Int, pageNumber: Int)

object FicsPageRequest:
  implicit val schema: Schema[FicsPageRequest] = DeriveSchema.gen
