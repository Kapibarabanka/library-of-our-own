package com.kapibarabanka.kapibarabot.tg

import cats.Parallel
import cats.effect.Async
import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.tg.stateProcessors.*
import com.kapibarabanka.kapibarabot.tg.services.{BotWithChatId, MyBotApi}
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.AppConfig.allowedChats
import com.kapibarabanka.kapibarabot.tg.models.{BotState, CommentBotState, ExistingFicBotState, NewFicBotState, SendToKindleBotState, StartBotState}
import telegramium.bots.*
import telegramium.bots.high.LongPollBot
import telegramium.bots.high.implicits.*
import zio.*

import scala.collection.mutable
import scala.collection.mutable.*

class Kapibarabot(bot: MyBotApi, ao3: Ao3, db: DbService)(implicit
    asyncF: Async[Task],
    parallel: Parallel[Task]
) extends LongPollBot[Task](bot.baseApi):
  private val statesByUsers: mutable.Map[String, BotState] = mutable.Map.empty[String, BotState]

  override def start(): Task[Unit] = for {
    _ <- ZIO.succeed(allowedChats.map(id => statesByUsers.addOne((id, StartBotState()))))
    _ <- super.start()
  } yield ()

  override def onMessage(msg: Message): Task[Unit] =
    useScenario(msg.chat.id.toString)(stateProcessor => stateProcessor.onMessage(msg))

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    useScenario(query.from.id.toString)(stateProcessor => stateProcessor.onCallbackQuery(query))

  private def useScenario(chatId: String)(getNextState: StateProcessor => Task[BotState]): Task[Unit] =
    statesByUsers.get(chatId) match
      case None => sendMessage(chatId = ChatStrId(chatId), text = "You are not in the allowed user list").exec(bot.baseApi).unit
      case Some(currentState) =>
        val currentStateProcessor = getStateProcessor(currentState, chatId)
        for {
          nextState <- getNextState(currentStateProcessor)
          _         <- if (nextState.performStartup) getStateProcessor(nextState, chatId).startup else ZIO.unit
          _ <- ZIO.succeed({
            statesByUsers(chatId) = nextState
          })
        } yield ()

  private def getStateProcessor(currentState: BotState, chatId: String) =
    val botWithChatId = BotWithChatId(chatId, bot)
    currentState match
      case state: CommentBotState      => CommentStateProcessor(state, botWithChatId, db)
      case state: ExistingFicBotState  => ExistingFicStateProcessor(state, botWithChatId, db)
      case state: NewFicBotState       => NewFicStateProcessor(state, botWithChatId, ao3, db)
      case state: SendToKindleBotState => SendToKindleStateProcessor(state, botWithChatId, db, ao3)
      case state: StartBotState        => StartStateProcessor(state, botWithChatId, db)
