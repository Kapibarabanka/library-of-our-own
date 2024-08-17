package com.kapibarabanka.airtable

enum AirtableError(msg: String) extends Exception(msg):
  case UnprocessableEntityError(msg: String, url: String) extends AirtableError(s"Error at $url:\n$msg")
  case ParsingError(msg: String, url: String)             extends AirtableError(s"Error at $url:\n$msg")
  case NotFoundError(msg: String, url: String)            extends AirtableError(s"Error at $url:\n$msg")
  case UnclassifiedError(msg: String, url: String)        extends AirtableError(s"Error at $url:\n$msg")
