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
