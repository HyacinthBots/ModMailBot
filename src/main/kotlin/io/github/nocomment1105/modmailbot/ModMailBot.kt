@file:OptIn(PrivilegedIntent::class)

package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.nocomment1105.modmailbot.database.DatabaseManager.startDatabase
import io.github.nocomment1105.modmailbot.extensions.MessageSending
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

	val bot = ExtensibleBot(config.getProperty("bot_token")) {
		applicationCommands {
			defaultGuild(config.getProperty("mail_server_id"))
		}

		extensions {
			add(::MessageSending)
		}

		intents {
			+Intent.GuildMembers
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

		hooks {
			afterKoinSetup {
				startDatabase()
			}
		}
	}

	bot.start()
}
