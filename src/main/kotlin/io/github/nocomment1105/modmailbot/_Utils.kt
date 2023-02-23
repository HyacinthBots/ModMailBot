/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.common.Color
import dev.kord.common.entity.DiscordPartialMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.collections.MetaCollection
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadCollection
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.koin.dsl.bind

/**
 * Creates an embed in a [EmbedBuilder] containing a message received from DM or sent from the mail guild.
 *
 * @param message The contents of the message to send
 * @author NoComment1105
 * @since 1.0.0
 */
fun EmbedBuilder.messageEmbed(message: Message) {
	author {
		name = message.author?.tag
		icon = message.author?.avatar!!.url
	}
	description = message.content
	timestamp = Clock.System.now()
	color = DISCORD_RED
	footer {
		text = "Message ID: ${message.id}"
	}
}

/**
 * Creates an embed in a [EmbedBuilder] containing a message from the mail guild to send to the DM of the user who
 * owns the thread channel. If the response is anonymous, the author of the embed will be detailed as the top role
 * of the user who send the message
 *
 * @param message The contents of the message to send
 * @param author The user who send the message in the mail guild
 * @param guildId Normally the mail guild id
 * @param embedColor The color to apply to the embed, defaults to [DISCORD_RED]
 * @param anonymous Whether to send an anonymous embed or not. Defaults to false
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun EmbedBuilder.messageEmbed(
	message: String,
	author: User,
	guildId: Snowflake,
	embedColor: Color? = null,
	anonymous: Boolean = false
) {
	author {
		if (!anonymous) {
			name = author.tag
			icon = author.avatar!!.url
		} else {
			name = author.asMember(MAIL_SERVER).getTopRole()!!.name
		}
	}
	description = message
	timestamp = Clock.System.now()
	color = embedColor ?: DISCORD_RED
	footer {
		text = author.asMember(guildId).getTopRole()!!.name
	}
}

/**
 * An embed builder set up to present an old and a new message in fields for use in the edit message functions.
 *
 * @param message The message
 * @param oldContent The old message content
 * @param newContent The new message content
 * @param embedColor An optional color to add to the embed
 *
 * @author NoComment1105
 * @since 1.0.0
 */
fun EmbedBuilder.editedMessageEmbed(
	message: Message,
	oldContent: String,
	newContent: String,
	embedColor: Color? = null
) {
	field {
		name = "Previous content"
		value = oldContent
	}
	field {
		name = "New content"
		value = newContent
	}
	timestamp = Clock.System.now()
	color = embedColor ?: DISCORD_RED
	footer {
		text = "Message ID: ${message.id}"
	}
}

/**
 * This function does all the necessary checks to see if the command context was within a channel that belongs to a
 * user, with an open channel. If it does not, an error is printed and null returned.
 *
 * @return The ID of the DM channel or null
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun EphemeralSlashCommandContext<*, *>.inThreadChannel(): Snowflake? =
	OpenThreadCollection().getDmFromThreadChannel(channel.id)

/**
 * This function sets up the database fully for use and runs migrations if requested.
 *
 * @param migrate Whether to migrate the database or not
 * @author NoComment1105
 * @since 1.0.0
 */
suspend inline fun ExtensibleBotBuilder.database(migrate: Boolean) {
	val db = Database()

	hooks {
		beforeKoinSetup {
			loadModule {
				single { db } bind Database::class
			}

			loadModule {
				single { MetaCollection() } bind MetaCollection::class
				single { OpenThreadCollection() } bind OpenThreadCollection::class
			}

			if (migrate) {
				runBlocking {
					db.migrate()
				}
			}
		}
	}
}

/**
 * Get this message's contents, trimmed to [desiredLength] characters long.
 * If the message exceeds that length, it will be truncated and an ellipsis appended.
 *
 * @param desiredLength The desired length to limit to
 * @author trainb0y
 */
fun Message?.trimmedContents(desiredLength: Int): String? {
	this ?: return null
	val useRegularLength = this.content.length < desiredLength.coerceIn(1, 1020)
	return if (this.content.length > desiredLength.coerceIn(1, 1020)) {
		this.content.substring(0, if (useRegularLength) this.content.length else desiredLength) + " ..."
	} else {
	    this.content
	}
}

/**
 * Get this message's contents, trimmed to [desiredLength] characters long.
 * If the message exceeds that length, it will be truncated and an ellipsis appended.
 *
 * @param desiredLength The desired length to limit to
 * @author trainb0y
 */
fun DiscordPartialMessage?.trimmedContents(desiredLength: Int): String? {
	this ?: return null
	val useRegularLength = this.content.value?.length!! < desiredLength.coerceIn(1, 1020)
	return if (this.content.value!!.length > desiredLength.coerceIn(1, 1020)) {
		this.content.value!!.substring(0, if (useRegularLength) this.content.value!!.length else desiredLength) + " ..."
	} else {
	    this.content.value
	}
}
