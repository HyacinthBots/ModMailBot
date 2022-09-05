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
