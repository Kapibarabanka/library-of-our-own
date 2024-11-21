package kapibarabanka.lo3.common

object AppConfig:
  val ao3Login: String    = sys.env("AO3_LOGIN")
  val ao3Password: String = sys.env("AO3_PASSWORD")

  val mainBotToken: String  = sys.env("MAIN_BOT")
  val adminBotToken: String = sys.env("ADMIN_BOT")
  val myChatId: String      = sys.env("MY_CHAT_ID")

  val senderEmail: String    = sys.env("SENDER_EMAIL")
  val senderPassword: String = sys.env("SENDER_PASSWORD")

  val dbPath: String   = sys.env("DB_PATH")
  val ficsPath: String = sys.env("DOWNLOADED_FICS")
  val dbName: String   = "kapibarabot.db"

  val htmlApi: String = sys.env("HTML_API")
  val dataApi: String = sys.env("DATA_API")
