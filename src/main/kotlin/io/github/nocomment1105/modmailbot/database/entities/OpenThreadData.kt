package io.github.nocomment1105.modmailbot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * The data for open threads in the database.
 *
 * @property userId The ID of the user who owns the thread
 * @property threadId The ID of the thread tied to the user
 *
 * @since 1.0.0
 */
@Serializable
data class OpenThreadData(
	val userId: Snowflake,
	val threadId: Snowflake
)
