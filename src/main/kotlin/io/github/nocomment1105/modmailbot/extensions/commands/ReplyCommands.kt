/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.commands

import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_GREEN
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import io.github.nocomment1105.modmailbot.database.collections.SentMessagesCollection
import io.github.nocomment1105.modmailbot.database.entities.SentMessageData
import io.github.nocomment1105.modmailbot.inThreadChannel
import io.github.nocomment1105.modmailbot.messageEmbed
import modmailbot.i18n.Translations

class ReplyCommands : Extension() {
	override val name = "reply-commands"

	override suspend fun setup() {
		ephemeralSlashCommand(::ReplyArgs) {
			name = Translations.Commands.Reply.Reply.name
			description = Translations.Commands.Reply.Reply.description

			check {
				// This is for the mailed to reply with not the mailer
				anyGuild()
			}

			action {
				val userToDm = inThreadChannel() ?: return@action

				val dmChannel = event.kord.getUser(userToDm)!!.getDmChannel()

				val dmChannelMessage = dmChannel.createMessage {
					embed {
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				val threadMessageId = channel.createMessage {
					embed {
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				SentMessagesCollection().addMessage(
					SentMessageData(
						channel.id,
						SentMessagesCollection().getNextMessageNumber(channel.id),
						dmChannelMessage.id,
						threadMessageId.id,
						wasSentByStaff = true,
						isAnonymous = false
					)
				)

				respond { content = Translations.Commands.Reply.sent.translate() }
			}
		}

		ephemeralSlashCommand(::ReplyArgs) {
			name = Translations.Commands.Reply.Anonreply.name
			description = Translations.Commands.Reply.Anonreply.name

			check {
				// This is for the mailed to reply with not the mailer
				anyGuild()
			}

			action {
				val userToDm = inThreadChannel() ?: return@action

				val dmChannel = event.kord.getUser(userToDm)!!.getDmChannel()

				val dmChannelMessage = dmChannel.createMessage {
					embed {
						// Send an anonymous response to the user
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN, true)
					}
				}

				val threadMessageId = channel.createMessage {
					embed {
						messageEmbed("(Anonymous) ${arguments.content}", user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				SentMessagesCollection().addMessage(
					SentMessageData(
						channel.id,
						SentMessagesCollection().getNextMessageNumber(channel.id),
						dmChannelMessage.id,
						threadMessageId.id,
						wasSentByStaff = true,
						isAnonymous = true
					)
				)

				respond { content = Translations.Commands.Reply.sent.translate() }
			}
		}
	}

	inner class ReplyArgs : Arguments() {
		val content by string {
			name = Translations.Commands.Reply.Reply.Args.Content.name
			description = Translations.Commands.Reply.Reply.Args.Content.description

			mutate {
				it.replace("\\n", "\n").replace("\n", "\n")
			}
		}
	}
}
