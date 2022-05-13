package io.github.nocomment1105.modmailbot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock

/**
 * Creates an embed in a [UserMessageCreateBuilder] containing a message received from DM or sent from the mail guild.
 *
 * @param message The contents of the message to send
 * @author NoComment1105
 * @since 1.0.0
 */
fun UserMessageCreateBuilder.messageEmbed(message: Message) {
	embed {
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
}
