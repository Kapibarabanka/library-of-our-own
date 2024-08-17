package com.kapibarabanka.ao3scrapper.exceptions

case class NotFoundException(identifier: String) extends Exception(s"Unable to find a $identifier")
