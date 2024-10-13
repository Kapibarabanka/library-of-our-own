package kapibarabanka.lo3.bot
package tg

import ao3scrapper.Ao3
import tg.models.*
import tg.processors.*
import tg.services.{AdminBot, BotWithChatId, MyBotApi}

import cats.Parallel
import cats.effect.Async
import telegramium.bots.*
import telegramium.bots.high.LongPollBot
import zio.*

import scala.collection.mutable
import scala.collection.mutable.*

class Kapibarabot(bot: MyBotApi, ao3: Ao3)(implicit
    asyncF: Async[Task],
    parallel: Parallel[Task]
) extends LongPollBot[Task](bot.baseApi):
  private val statesByUsers: mutable.Map[String, BotState] = mutable.Map.empty[String, BotState]

  override def start(): Task[Unit] = for {
    allUsers <- db.users.getAllIds
    _        <- ZIO.succeed(allUsers.map(id => statesByUsers.addOne((id, StartBotState()))))
    _        <- db.init
    _        <- super.start()
  } yield ()

  override def onMessage(msg: Message): Task[Unit] =
    useScenario(msg.chat.id.toString, msg.chat.username)(stateProcessor => stateProcessor.onMessage(msg))

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    useScenario(query.from.id.toString, query.from.username)(stateProcessor => stateProcessor.onCallbackQuery(query))

  private def useScenario(chatId: String, username: Option[String])(getNextState: StateProcessor => Task[BotState]): Task[Unit] =
    for {
      currentState          <- getOrCreateCurrentState(chatId, username)
      currentStateProcessor <- ZIO.succeed(getStateProcessor(currentState, chatId))
      nextState             <- getNextState(currentStateProcessor)
      _                     <- if (nextState.performStartup) getStateProcessor(nextState, chatId).startup else ZIO.unit
      _ <- ZIO.succeed({
        statesByUsers(chatId) = nextState
      })
    } yield ()

  private def getOrCreateCurrentState(chatId: String, username: Option[String]) =
    statesByUsers.get(chatId) match
      case Some(currentState) => ZIO.succeed(currentState)
      case None =>
        for {
          _          <- db.users.addUser(chatId, username)
          _          <- AdminBot.newUserAlert(chatId, username)
          startState <- ZIO.succeed(StartBotState())
          _          <- ZIO.succeed(statesByUsers.addOne((chatId, startState)))
        } yield startState

  private def getStateProcessor(currentState: BotState, chatId: String) =
    val botWithChatId = BotWithChatId(chatId, bot)
    currentState match
      case state: CommentBotState      => CommentStateProcessor(state, botWithChatId)
      case state: ExistingFicBotState  => ExistingFicStateProcessor(state, botWithChatId)
      case state: NewFicBotState       => NewFicStateProcessor(state, botWithChatId, ao3)
      case state: SendToKindleBotState => SendToKindleStateProcessor(state, botWithChatId, ao3)
      case state: StartBotState        => StartStateProcessor(state, botWithChatId)
      case state: SetEmailBotState     => SetEmailStateProcessor(state, botWithChatId)
      case state: FeedbackBotState     => FeedbackStateProcessor(state, botWithChatId)
