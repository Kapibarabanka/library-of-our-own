package kapibarabanka.lo3.bot
package services

import models.*
import processors.*

import cats.Parallel
import cats.effect.Async
import kapibarabanka.lo3.common.lo3api.UserClient
import kapibarabanka.lo3.common.services.{BotWithChatId, MyBotApi}
import telegramium.bots.*
import telegramium.bots.high.LongPollBot
import zio.*

import scala.collection.mutable
import scala.collection.mutable.*

class Lo3bot(bot: MyBotApi, api: Lo3Api)(implicit
    asyncF: Async[Task],
    parallel: Parallel[Task]
) extends LongPollBot[Task](bot.baseApi):
  private val statesByUsers: mutable.Map[String, BotState] = mutable.Map.empty[String, BotState]

  override def start(): Task[Unit] = for {
    allUsers <- api.run(UserClient.allIds({}))
    _        <- ZIO.succeed(allUsers.map(id => statesByUsers.addOne((id, StartBotState()))))
    _        <- super.start()
  } yield ()

  override def onMessage(msg: Message): Task[Unit] =
    useScenario(msg.chat.id.toString, msg.chat.username)(stateProcessor =>
      stateProcessor.onMessage(msg).provide(ZLayer { ZIO.succeed(api) })
    )

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    useScenario(query.from.id.toString, query.from.username)(stateProcessor =>
      stateProcessor.onCallbackQuery(query).provide(ZLayer { ZIO.succeed(api) })
    )

  private def useScenario(chatId: String, username: Option[String])(getNextState: StateProcessor => Task[BotState]): Task[Unit] =
    for {
      currentState          <- getOrCreateCurrentState(chatId, username)
      currentStateProcessor <- ZIO.succeed(getStateProcessor(currentState, chatId))
      nextState             <- getNextState(currentStateProcessor)
      _ <-
        if (nextState.performStartup) getStateProcessor(nextState, chatId).startup.provide(ZLayer { ZIO.succeed(api) })
        else ZIO.unit
      _ <- ZIO.succeed({
        statesByUsers(chatId) = nextState
      })
    } yield ()

  private def getOrCreateCurrentState(chatId: String, username: Option[String]) =
    statesByUsers.get(chatId) match
      case Some(currentState) => ZIO.succeed(currentState)
      case None =>
        for {
          _          <- api.run(UserClient.add(chatId, username))
          _          <- AdminBot.newUserAlert(chatId, username)
          startState <- ZIO.succeed(StartBotState())
          _          <- ZIO.succeed(statesByUsers.addOne((chatId, startState)))
        } yield startState

  private def getStateProcessor(currentState: BotState, chatId: String) =
    val botWithChatId = BotWithChatId(chatId, bot)
    currentState match
      case state: CommentBotState     => CommentStateProcessor(state, botWithChatId)
      case state: ExistingFicBotState => ExistingFicStateProcessor(state, botWithChatId)
      case state: StartBotState       => StartStateProcessor(state, botWithChatId)
      case state: SetEmailBotState    => SetEmailStateProcessor(state, botWithChatId)
      case state: FeedbackBotState    => FeedbackStateProcessor(state, botWithChatId)
