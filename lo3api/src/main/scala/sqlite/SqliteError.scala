package kapibarabanka.lo3.api
package sqlite

import zio.schema.{DeriveSchema, Schema}

enum SqliteError(message: String) extends Exception(message):
  case DbInitError(message: String)     extends SqliteError(message)
  case DbActionError(message: String)   extends SqliteError(message)
  case CantConnectToDb(message: String) extends SqliteError(message)

object SqliteError {
  implicit val schema: Schema[SqliteError] = DeriveSchema.gen
}
