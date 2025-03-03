package kapibarabanka.lo3.bot
package models

import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.Fic

sealed trait BotState:
  val performStartup: Boolean

case class SetEmailBotState() extends BotState:
  override val performStartup: Boolean = true

case class FeedbackBotState() extends BotState:
  override val performStartup: Boolean = true

case class CommentBotState(ficForComment: Fic) extends BotState:
  override val performStartup: Boolean = true

case class ExistingFicBotState(displayedFic: Fic, performStartup: Boolean) extends BotState:
  def withoutStartup: ExistingFicBotState = this.copy(performStartup = false)

case class StartBotState(performStartup: Boolean = false) extends BotState
