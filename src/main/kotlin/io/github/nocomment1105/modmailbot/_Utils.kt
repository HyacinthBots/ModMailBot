package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.getTopRole
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.nocomment1105.modmailbot.database.DatabaseManager
import io.github.nocomment1105.modmailbot.database.getDmFromThreadChannel
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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
 * This function does all the necessary checks to see if the command context was within a channel that belongs to a
 * user, with an open channel. If it does not, an error is printed and null returned.
 *
 * @return The ID of the DM channel or null
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun EphemeralSlashCommandContext<*>.inThreadChannel(): String? {
	var userToDm: String? = null
	newSuspendedTransaction {
		userToDm = try {
			getDmFromThreadChannel(channel.id, DatabaseManager.OpenThreads.userId)
		} catch (e: NoSuchElementException) {
			respond {
				content = "**Error**: This channel does not belong to a user! Use this command in user " +
						"channels only"
			}
			null
		}
	}
	return if (userToDm != null) {
		userToDm
	} else {
		null
	}
}
