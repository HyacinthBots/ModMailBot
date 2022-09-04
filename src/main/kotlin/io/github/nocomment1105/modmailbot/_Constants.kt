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
