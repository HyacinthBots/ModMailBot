/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot

import dev.kordex.core.utils.env
import dev.kordex.core.utils.envOrNull

/** The token of the bot. */
val BOT_TOKEN: String = env("BOT_TOKEN")

/** The URI to connect to the database. */
val MONGO_URI = envOrNull("MONGO_URI") ?: "mongodb://localhost:27017"
