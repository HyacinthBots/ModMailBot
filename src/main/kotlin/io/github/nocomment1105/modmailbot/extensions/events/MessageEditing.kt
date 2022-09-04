/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.events

import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessageCollection
import io.github.nocomment1105.modmailbot.editedMessageEmbed
import io.github.nocomment1105.modmailbot.trimmedContents

class MessageEditing : Extension() {
	override val name = "message-editing"

	override suspend fun setup() {
		event<MessageUpdateEvent> {
			check {
				noGuild()
				failIf(event.getMessage().author?.id == kord.selfId || event.getMessage().author == null)
			}

			action {
				val userThread = OpenThreadCollection().getOpenThreadsForUser(event.getMessage().author!!.id)
				val threadMessageIdToEdit =
					SentMessageCollection().getInternalMessageById(userThread!!.threadId, event.messageId)!!
				val threadMessageToEdit =
					kord.getGuild(MAIL_SERVER)!!.getChannelOf<GuildMessageChannel>(userThread.threadId)
						.getMessage(threadMessageIdToEdit)

				threadMessageToEdit.edit {
					embed {
						editedMessageEmbed(
							event.getMessage(),
							event.old?.trimmedContents(512) ?: "Failed to get content of message",
							event.new.trimmedContents(512) ?: "Failed to get content of message"
						)
					}
				}

				event.message.addReaction(Emojis.pencil)
			}
		}

		ephemeralSlashCommand(::EditArgs) {
			name = "edit"
			description = "Edit a message you sent"

			action {
				// TODO edit the message on the user end, no copy of original
			}
		}
	}

	inner class EditArgs : Arguments() {
		val messageId by snowflake {
			name = "message-id"
			description = "The ID of the message you're editing"
		}
	}
}
