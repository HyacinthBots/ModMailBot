package io.github.nocomment1105.modmailbot.extensions.events

import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.message.MessageUpdateEvent

class MessageEditing : Extension() {
	override val name = "message-editing"

	override suspend fun setup() {
		event<MessageUpdateEvent> {
			check {
				noGuild()
			}

			action {
				// TODO Do message updating on the staff end, keep copy of original
			}
		}

		ephemeralSlashCommand {
			name = "edit"
			description = "Edit a message you sent"

			action {
				// TODO edit the message on the user end, no copy of original
			}
		}
	}
}
