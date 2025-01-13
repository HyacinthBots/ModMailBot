/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.events

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.checks.noGuild
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.time.TimestampType
import dev.kordex.core.time.toDiscord
import dev.kordex.core.utils.createdAt
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.MAIN_SERVER
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadsCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessagesCollection
import io.github.nocomment1105.modmailbot.database.entities.OpenThreadData
import io.github.nocomment1105.modmailbot.database.entities.SentMessageData
import io.github.nocomment1105.modmailbot.messageEmbed
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import modmailbot.i18n.Translations

class MessageReceiving : Extension() {

	override val name = "message-receiving"

	override suspend fun setup() {
		event<MessageCreateEvent> {
			check {
				noGuild()
				failIf(event.message.author?.id == kord.selfId || event.message.author == null)
			}
			action {
				// Check to see if the user has any threads open already
				val openThread: Boolean =
					OpenThreadsCollection().getOpenThreadsForUser(event.message.author!!.id) != null

				val mailChannel: TextChannel

				val translations = Translations.Events.Receiving

				if (!openThread) {
					// Get the mail channel
					mailChannel = kord.getGuildOrNull(MAIL_SERVER)!!.createTextChannel(event.message.author!!.tag)

					// Store the users thread in the database
					OpenThreadsCollection().add(
						OpenThreadData(
							userId = event.message.author!!.id,
							threadId = mailChannel.id
						)
					)

					mailChannel.createMessage {
						content = "@here" // TODO Implement a config options system
						// Provide some information about the user in an initial embed
						embed {
							description =
								translations.description.translate(event.message.author!!.mention) +
									event.message.author!!.fetchUser().createdAt.toDiscord(TimestampType.LongDateTime)
							timestamp = Clock.System.now()
							color = DISCORD_BLURPLE

							field {
								name = translations.nickname.translate()
								value = event.message.author!!.asMember(MAIN_SERVER).nickname
									?: Translations.Utils.none.translate()
								inline = true
							}

							field {
								val roles = event.message.author!!.asMember(MAIN_SERVER).roles.toList().map { it }
								name = translations.roles.translate()
								value = if (roles.isEmpty()) {
									Translations.Utils.none.translate()
								} else {
									"${roles.forEach { "${it.name}\n" }}"
								}
								inline = true
							}

							author {
								name = event.message.author?.tag
								icon = event.message.author?.avatar?.cdnUrl?.toUrl()
							}

							footer {
								text = translations.footer.translate(
									event.message.author!!.id,
									event.message.author!!.getDmChannel().id
								)
							}
						}
					}

					val mailChannelMessage = mailChannel.createMessage {
						// Send the message through to the mail server
						embed {
							messageEmbed(event.message)
						}
					}

					SentMessagesCollection().addMessage(
						SentMessageData(
							mailChannel.id,
							SentMessagesCollection().getNextMessageNumber(mailChannel.id),
							event.message.id,
							mailChannelMessage.id,
							wasSentByStaff = false,
							false
						)
					)

					// React to the message in DMs with a white_check_mark, once the message is sent to the mail sever
					event.message.addReaction(Emojis.whiteCheckMark)
				} else {
					// Get the mail server from the config file
					mailChannel = kord.getGuildOrNull(MAIL_SERVER)!!.getChannelOf(
						OpenThreadsCollection().getOpenThreadsForUser(event.message.author!!.id)!!.threadId
					)

					// Send the user's message through to the mail server
					val mailChannelMessage = mailChannel.createMessage {
						embed {
							messageEmbed(event.message)
						}
					}

					SentMessagesCollection().addMessage(
						SentMessageData(
							mailChannel.id,
							SentMessagesCollection().getNextMessageNumber(mailChannel.id),
							event.message.id,
							mailChannelMessage.id,
							false,
							isAnonymous = false
						)
					)

					// React to the message in DMs with a white_check_mark, once the message is sent to the mail sever
					event.message.addReaction(Emojis.whiteCheckMark)
				}
			}
		}
	}
}
