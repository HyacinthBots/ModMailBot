/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot

import dev.kord.common.entity.Snowflake

/** The token of the bot. */
val BOT_TOKEN: String = config.getProperty("bot_token")

/** The ID of the mail server. */
val MAIL_SERVER = Snowflake(config.getProperty("mail_server_id"))

/** The ID of the main server. */
val MAIN_SERVER = Snowflake(config.getProperty("main_server_id"))

/** The URI to connect to the database. */
val MONGO_URI = config.getProperty("mongo_uri") ?: "mongodb://localhost:27017"
