package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.getTopRole
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock

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
 * owns the thread channel.
 *
 * @param message The contents of the message to send
 * @param author The user who send the message in the mail guild
 * @param guildId Normally the mail guild id
 * @param embedColor The color to apply to the embed, defaults to [DISCORD_RED]
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun EmbedBuilder.messageEmbed(message: String, author: User, guildId: Snowflake, embedColor: Color? = null) {
	author {
		name = author.tag
		icon = author.avatar!!.url
	}
	description = message
	timestamp = Clock.System.now()
	color = embedColor ?: DISCORD_RED
	footer {
		text = author.asMember(guildId).getTopRole()!!.name
	}
}
