/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.events

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.utils.createdAt
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.MAIN_SERVER
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessageCollection
import io.github.nocomment1105.modmailbot.database.entities.OpenThreadData
import io.github.nocomment1105.modmailbot.database.entities.SentMessageData
import io.github.nocomment1105.modmailbot.messageEmbed
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import mu.KotlinLogging

class MessageReceiving : Extension() {

	override val name = "message-receiving"

	override suspend fun setup() {
		val logger = KotlinLogging.logger("Message Receiving")

		event<MessageCreateEvent> {
			check {
				noGuild()
				failIf(event.message.author?.id == kord.selfId || event.message.author == null)
			}
			action {
				// Check to see if the user has any threads open already
				val openThread: Boolean =
					OpenThreadCollection().getOpenThreadsForUser(event.message.author!!.id) != null

				val mailChannel: TextChannel

				if (!openThread) {
					// Get the mail channel
					mailChannel = kord.getGuild(MAIL_SERVER)!!.createTextChannel(event.message.author!!.tag)

					// Store the users thread in the database
					OpenThreadCollection().add(
						OpenThreadData(
							userId = event.message.author!!.id,
							threadId = mailChannel.id
						)
					)

					mailChannel.createMessage {
						content = "@here" // TODO Implement a config options system
						// Provide some information about the user in an initial embed
						embed {
							description = "${event.message.author!!.mention} was created " +
									event.message.author!!.fetchUser().createdAt.toDiscord(TimestampType.LongDateTime)
							timestamp = Clock.System.now()
							color = DISCORD_BLURPLE

							field {
								name = "Nickname"
								value = event.message.author!!.asMember(MAIN_SERVER).nickname.toString()
								inline = true
							}

							field {
								val roles = event.message.author!!.asMember(MAIN_SERVER).roles.toList().map { it }
								name = "Roles"
								roles.forEach {
									value += "${it.name}\n"
								}
								inline = true
							}

							author {
								name = event.message.author?.tag
								icon = event.message.author?.avatar!!.url
							}

							footer {
								text = "User ID: ${event.message.author!!.id} |" +
										" DM ID: ${event.message.author!!.getDmChannel().id}"
							}
						}
					}

					val mailChannelMessage = mailChannel.createMessage {
						// Send the message through to the mail server
						embed {
							messageEmbed(event.message)
						}
					}

					SentMessageCollection().addMessage(
						SentMessageData(
							mailChannel.id,
							SentMessageCollection().getNextMessageNumber(mailChannel.id),
							event.message.id,
							mailChannelMessage.id,
							false
						)
					)

					// React to the message in DMs with a white_check_mark, once the message is sent to the mail sever
					event.message.addReaction(Emojis.whiteCheckMark)
				} else {
					// Get the mail server from the config file
					mailChannel = kord.getGuild(MAIL_SERVER)!!.getChannelOf(
						OpenThreadCollection().getOpenThreadsForUser(event.message.author!!.id)!!.threadId
					)

					// Send the user's message through to the mail server
					val mailChannelMessage = mailChannel.createMessage {
						embed {
							messageEmbed(event.message)
						}
					}

					SentMessageCollection().addMessage(
						SentMessageData(
							mailChannel.id,
							SentMessageCollection().getNextMessageNumber(mailChannel.id),
							event.message.id,
							mailChannelMessage.id,
							false
						)
					)

					// React to the message in DMs with a white_check_mark, once the message is sent to the mail sever
					event.message.addReaction(Emojis.whiteCheckMark)
				}
			}
		}
	}
}
