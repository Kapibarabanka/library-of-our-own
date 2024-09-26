package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.kapibarabot.domain.UserFicRecord

sealed trait BotState:
  val performStartup: Boolean
  
case class CommentBotState(ficForComment: UserFicRecord) extends BotState:
  override val performStartup: Boolean = true
  
case class ExistingFicBotState(displayedFic: UserFicRecord, performStartup: Boolean) extends BotState:
  def withoutStartup = this.copy(performStartup = false)

case class NewFicBotState(ao3Link: String) extends BotState:
  override val performStartup: Boolean = true
  
case class SendToKindleBotState(ficToSend: UserFicRecord, userEmail: String) extends BotState:
  override val performStartup: Boolean = true
  
case class StartBotState() extends BotState:
  override val performStartup: Boolean = false
