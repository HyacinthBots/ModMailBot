/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.events

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessageCollection
import io.github.nocomment1105.modmailbot.editedMessageEmbed
import io.github.nocomment1105.modmailbot.messageEmbed
import io.github.nocomment1105.modmailbot.trimmedContents
import kotlin.time.Duration.Companion.seconds

class MessageEditing : Extension() {
	override val name = "message-editing"

	@OptIn(UnsafeAPI::class)
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
						author {
							name = event.getMessage().author?.tag
							icon = event.getMessage().author?.avatar?.url
						}
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

		unsafeSlashCommand(::EditArgs) {
			name = "edit"
			description = "Edit a message you sent"

			initialResponse = InitialSlashCommandResponse.None

			action {
				val userDmId = OpenThreadCollection().getDmFromThreadChannel(channel.id)
				val dmMessageIdToEdit = SentMessageCollection().getDmMessageById(channel.id, arguments.messageId)
				val dmMessageToEdit = event.kord.getUser(userDmId!!)!!.getDmChannel().getMessage(dmMessageIdToEdit!!)
				val threadMessageToEdit = channel.getMessage(arguments.messageId)
				// Get all messages from the thread and find the one that has the same ID as the message being edited
				val originalSentMessage = SentMessageCollection().getMessagesInThread(channel.id).find {
					it?.threadMessageId == arguments.messageId
				}

				val originalMessageEmbed = dmMessageToEdit.embeds[0]

				val response = event.interaction.modal("Edit message", "editMessageModal") {
					actionRow {
						textInput(TextInputStyle.Paragraph, "editInput", "New message content") {
							value = originalMessageEmbed.description
						}
					}
				}

				val interaction =
					response.kord.waitFor<ModalSubmitInteractionCreateEvent>(120.seconds.inWholeMilliseconds) {
						interaction.modalId == "editMessageModal"
					}?.interaction ?: run {
						response.createEphemeralFollowup {
							embed {
								description = "Editing timed out"
							}
						}
						return@action
					}

				val newMessageContent = interaction.textInputs["editInput"]!!.value!!
				val modalResponse = interaction.deferEphemeralResponse()

				threadMessageToEdit.edit {
					embed {
						author {
							name = user.asUser().tag
							icon = user.asUser().avatar?.url
						}
						editedMessageEmbed(
							dmMessageToEdit,
							originalMessageEmbed.description ?: "Failed to get original message content",
							newMessageContent,
							DISCORD_GREEN
						)
					}
				}

				dmMessageToEdit.edit {
					embed {
						messageEmbed(
							newMessageContent,
							user.asUser(),
							MAIL_SERVER,
							DISCORD_GREEN,
							originalSentMessage?.isAnonymous ?: false
						)
					}
				}

				modalResponse.respond {
					content = "Message edited"
				}
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
