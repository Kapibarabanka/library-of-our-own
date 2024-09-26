package com.kapibarabanka.kapibarabot.utils

object Config:
  val ao3Login: String    = sys.env("AO3_LOGIN")
  val ao3Password: String = sys.env("AO3_PASSWORD")

  val tgToken: String            = sys.env("TG_TOKEN")
  val allowedChats: List[String] = sys.env("ALLOWED_CHATS").split(",").toList
  val myChatId: String           = sys.env("MY_CHAT_ID")

  val senderEmail: String    = sys.env("SENDER_EMAIL")
  val senderPassword: String = sys.env("SENDER_PASSWORD")
  val kindleEmail: String    = sys.env("KINDLE_EMAIL")
  val tempDir: String        = sys.env("FICS_TEMP_PATH")

  val dbPath: String = sys.env("DB_PATH")
  val dbName: String = sys.env("DB_NAME")
