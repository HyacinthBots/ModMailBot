/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

@file:OptIn(PrivilegedIntent::class)

package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.cache.map.MapLikeCollection
import dev.kord.cache.map.internal.MapEntryCache
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.nocomment1105.modmailbot.extensions.commands.CloseCommands
import io.github.nocomment1105.modmailbot.extensions.commands.ReplyCommands
import io.github.nocomment1105.modmailbot.extensions.events.MessageEditing
import io.github.nocomment1105.modmailbot.extensions.events.MessageReceiving
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.util.*

val file = FileInputStream("config.properties")
val config = Properties()

suspend fun main() {
	withContext(Dispatchers.IO) {
		config.load(file)
	}

	val bot = ExtensibleBot(BOT_TOKEN) {
		database(false)

		applicationCommands {
			defaultGuild(MAIL_SERVER)
		}

		extensions {
			add(::MessageReceiving)
			add(::ReplyCommands)
			add(::MessageEditing)
			add(::CloseCommands)
		}

		intents {
			+Intent.GuildMembers // Privileged intent
			+Intent.MessageContent // Privileged intent

			+Intent.DirectMessages
			+Intent.GuildMessages
			+Intent.Guilds
			+Intent.GuildVoiceStates
			+Intent.GuildMessageTyping
			+Intent.DirectMessageTyping
		}

		presence {
			when (config.getProperty("statusType")) {
				"playing" -> playing(config.getProperty("status"))
				"watching" -> watching(config.getProperty("status"))
				else -> watching("for your DMs!")
			}
		}

		kord {
			cache {
				messages { cache, description ->
					MapEntryCache(cache, description, MapLikeCollection.concurrentHashMap())
				}
			}
		}
	}

	bot.start()
}
