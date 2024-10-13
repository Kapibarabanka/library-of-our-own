package kapibarabanka.lo3.bot

object AppConfig:
  val ao3Login: String    = sys.env("AO3_LOGIN")
  val ao3Password: String = sys.env("AO3_PASSWORD")

  val mainBotToken: String       = sys.env("MAIN_BOT")
  val adminBotToken: String      = sys.env("ADMIN_BOT")
  val allowedChats: List[String] = sys.env("ALLOWED_CHATS").split(",").toList
  val myChatId: String           = sys.env("MY_CHAT_ID")

  val senderEmail: String    = sys.env("SENDER_EMAIL")
  val senderPassword: String = sys.env("SENDER_PASSWORD")
  val tempDir: String        = sys.env("FICS_TEMP_PATH")

  val dbPath: String = sys.env("DB_PATH")
  val dbName: String = "kapibarabot.db"

  val htmlApi: String = sys.env("HTML_API")
