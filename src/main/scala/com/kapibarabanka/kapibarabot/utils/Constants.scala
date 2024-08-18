package com.kapibarabanka.kapibarabot.utils

import telegramium.bots.ChatIntId

object Constants {
  val entityBaseUrl: String               = s"https://airtable.com/appx80f0D1MARDMO3/tblyhNtSAcxhYE3xZ/viww5I0k5m3tyEN70/"
  def tgFileUrl(filePath: String): String = s"https://api.telegram.org/file/bot${sys.env("TG_TOKEN")}/$filePath"
  val myChatId: ChatIntId                 = ChatIntId(sys.env("CHAT_ID").toLong)
  val tempDir: String                     = sys.env("FICS_TEMP_PATH")
}
