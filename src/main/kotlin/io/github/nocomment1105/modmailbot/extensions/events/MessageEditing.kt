/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.events

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import dev.kordex.core.DISCORD_GREEN
import dev.kordex.core.checks.noGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.snowflake
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadsCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessagesCollection
import io.github.nocomment1105.modmailbot.editedMessageEmbed
import io.github.nocomment1105.modmailbot.messageEmbed
import io.github.nocomment1105.modmailbot.trimmedContents
import modmailbot.i18n.Translations

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
				val userThread = OpenThreadsCollection().getOpenThreadsForUser(event.getMessage().author!!.id)
				val threadMessageIdToEdit =
					SentMessagesCollection().getInternalMessageById(userThread!!.threadId, event.messageId)!!
				val threadMessageToEdit =
					kord.getGuildOrNull(MAIL_SERVER)!!.getChannelOf<GuildMessageChannel>(userThread.threadId)
						.getMessage(threadMessageIdToEdit)

				threadMessageToEdit.edit {
					embed {
						author {
							name = event.getMessage().author?.tag
							icon = event.getMessage().author?.avatar?.cdnUrl?.toUrl()
						}
						editedMessageEmbed(
							event.getMessage(),
							event.old?.trimmedContents(512) ?: Translations.Events.Edit.contentFailed.translate(),
							event.new.trimmedContents(512) ?: Translations.Events.Edit.contentFailed.translate()
						)
					}
				}

				event.message.addReaction(Emojis.pencil)
			}
		}

		ephemeralSlashCommand(::EditArgs, ::EditModal) {
			name = Translations.Events.Edit.name
			description = Translations.Events.Edit.description

			locking = true

			action { modal ->
				val userDmId = OpenThreadsCollection().getDmFromThreadChannel(channel.id)
				val dmMessageIdToEdit = SentMessagesCollection().getDmMessageById(channel.id, arguments.messageId)
				val dmMessageToEdit = event.kord.getUser(userDmId!!)!!.getDmChannel().getMessage(dmMessageIdToEdit!!)
				val threadMessageToEdit = channel.getMessage(arguments.messageId)
				// Get all messages from the thread and find the one that has the same ID as the message being edited
				val originalSentMessage = SentMessagesCollection().getMessagesInThread(channel.id).find {
					it?.threadMessageId == arguments.messageId
				}

				val originalMessageEmbed = dmMessageToEdit.embeds[0]

				EditModal().newContents.initialValue = originalMessageEmbed.description?.toKey()

				threadMessageToEdit.edit {
					embed {
						author {
							name = user.asUser().tag
							icon = user.asUser().avatar?.cdnUrl?.toUrl()
						}
						editedMessageEmbed(
							dmMessageToEdit,
							originalMessageEmbed.description ?: Translations.Events.Edit.contentFailed.translate(),
							modal?.newContents.toString(),
							DISCORD_GREEN
						)
					}
				}

				dmMessageToEdit.edit {
					embed {
						messageEmbed(
							modal?.newContents.toString(),
							user.asUser(),
							MAIL_SERVER,
							DISCORD_GREEN,
							originalSentMessage?.isAnonymous == true
						)
					}
				}

				respond {
					content = Translations.Events.Edit.edited.translate()
				}
			}
		}
	}

	inner class EditArgs : Arguments() {
		val messageId by snowflake {
			name = Translations.Events.Edit.Args.MessageId.name
			description = Translations.Events.Edit.Args.MessageId.description
		}
	}

	inner class EditModal : ModalForm() {
		override var title: Key = Translations.Events.Edit.Modal.title

		val newContents = paragraphText {
			label = Translations.Events.Edit.Modal.NewContents.label
		}
	}
}
