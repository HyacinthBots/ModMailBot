package io.github.nocomment1105.modmailbot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class SentMessageData(
	val threadId: Snowflake,
	val messageNumber: Int,
	val dmMessageId: Snowflake,
	val threadMessageId: Snowflake,
	val wasSentByStaff: Boolean
)
