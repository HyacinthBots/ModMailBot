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

@Serializable
data class SentMessageData(
	val threadId: Snowflake,
	val messageNumber: Int,
	val dmMessageId: Snowflake,
	val threadMessageId: Snowflake,
	val wasSentByStaff: Boolean
)
