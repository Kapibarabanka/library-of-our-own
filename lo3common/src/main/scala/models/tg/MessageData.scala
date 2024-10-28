package kapibarabanka.lo3.common
package models.tg

import telegramium.bots.*

case class MessageData(
    text: String,
    replyMarkup: Option[InlineKeyboardMarkup] = Option.empty,
    parseMode: Option[ParseMode] = Some(Html),
    entities: List[MessageEntity] = List.empty,
    linkPreviewOptions: Option[LinkPreviewOptions] = Some(LinkPreviewOptions(isDisabled = Some(true))),
    businessConnectionId: Option[String] = Option.empty,
    messageThreadId: Option[Int] = Option.empty,
    disableNotification: Option[Boolean] = Option.empty,
    protectContent: Option[Boolean] = Option.empty,
    messageEffectId: Option[String] = Option.empty,
    replyParameters: Option[ReplyParameters] = Option.empty,
    inlineMessageId: Option[String] = Option.empty
)
