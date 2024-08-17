package com.kapibarabanka.ao3scrapper.exceptions

case class RestrictedWorkException(id: String) extends Exception(s"Work with id $id is restricted to registered users")
