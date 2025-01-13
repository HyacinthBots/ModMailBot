/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * The data for threads that are queued for closure. Note threads that are immediately closed do not pass through this
 * database table.
 *
 * @property threadId The ID of the thread
 * @property userId The owner of the thread
 * @property timeToClose The time the thread needs closing
 * @property silently Whether to close silently or not
 * @property message The message to send with the closure, or null
 *
 * @since 1.0.0
 */
@Serializable
data class CloseQueueData(
	val threadId: Snowflake,
	val userId: Snowflake,
	val timeToClose: Instant,
	val silently: Boolean,
	val message: String?
)
