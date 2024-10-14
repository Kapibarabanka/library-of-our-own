package kapibarabanka.lo3.api
package ao3scrapper

enum Ao3Error(msg: String) extends Exception(msg) {
  case AuthFailed() extends Ao3Error("Failed to authenticate")

  case TooManyRequests() extends Ao3Error("HTTP status 429: Too many requests")

  case NotFound(entityName: String) extends Ao3Error(s"Couldn't find $entityName on Ao3")

  case DownloadLinkNotFound(workId: String) extends Ao3Error(s"Couldn't find download link for work $workId")

  case HttpError(status: Int, actionName: String) extends Ao3Error(s"Got HTTP status $status while $actionName")

  case ParsingError(msg: String, attemptedEntityName: String) extends Ao3Error(s"Error while parsing $attemptedEntityName:\n$msg")

  case UnspecifiedError(message: String) extends Ao3Error(message)
}
