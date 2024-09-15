package com.kapibarabanka.ao3scrapper

enum Ao3ClientError(msg: String) extends Exception(msg) {
  case AuthFailed extends Ao3ClientError("Failed to authenticate")
  case Restricted(ficType: "Work" | "Series", id: String)
      extends Ao3ClientError(
        s"$ficType with id $id is restricted to registered users. Please use authenticated client to parse it"
      )
  case NotFound(entityType: String, id: String) extends Ao3ClientError(s"Unable to find a $entityType with $id")
  case HttpError(message: String)               extends Ao3ClientError(message)
  case LinkNotFound(workId: String)             extends Ao3ClientError(s"Couldn't find download link for work $workId")
}
