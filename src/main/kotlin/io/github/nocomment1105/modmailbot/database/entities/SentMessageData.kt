/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * The data for messages sent in a modmail thread.
 *
 * @property threadId The ID of the thread the message was sent in
 * @property messageNumber The number of the message sent
 * @property dmMessageId The ID of the message on the DM side
 * @property threadMessageId The ID of the message on the thread side
 * @property wasSentByStaff Whether the message was sent by a staff member
 * @property isAnonymous Whether the message was sent as anonymous
 *
 * @since 1.0.0
 */
@Serializable
data class SentMessageData(
	val threadId: Snowflake,
	val messageNumber: Int,
	val dmMessageId: Snowflake,
	val threadMessageId: Snowflake,
	val wasSentByStaff: Boolean,
	val isAnonymous: Boolean
)
