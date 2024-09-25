package com.kapibarabanka.kapibarabot.utils

object Constants {
  val entityBaseUrl: String               = s"https://airtable.com/appx80f0D1MARDMO3/tblyhNtSAcxhYE3xZ/viww5I0k5m3tyEN70/"
  def tgFileUrl(filePath: String): String = s"https://api.telegram.org/file/bot${Config.tgToken}/$filePath"
}
