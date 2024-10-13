package kapibarabanka.lo3.bot
package tg.models

import ao3scrapper.domain.FicType
import domain.UserFicRecord

sealed trait BotState:
  val performStartup: Boolean

case class SetEmailBotState() extends BotState:
  override val performStartup: Boolean = true

case class FeedbackBotState() extends BotState:
    override val performStartup: Boolean = true

case class CommentBotState(ficForComment: UserFicRecord) extends BotState:
  override val performStartup: Boolean = true

case class ExistingFicBotState(displayedFic: UserFicRecord, performStartup: Boolean) extends BotState:
  def withoutStartup: ExistingFicBotState = this.copy(performStartup = false)

case class NewFicBotState(ficId: String, ficType: FicType) extends BotState:
  override val performStartup: Boolean = true

case class SendToKindleBotState(ficToSend: UserFicRecord, userEmail: String) extends BotState:
  override val performStartup: Boolean = true

case class StartBotState(performStartup: Boolean = false) extends BotState
