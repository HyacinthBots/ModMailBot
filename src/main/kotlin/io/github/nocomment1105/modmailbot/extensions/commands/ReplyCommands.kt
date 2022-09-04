/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.extensions.commands

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.checks.inGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.inThreadChannel
import io.github.nocomment1105.modmailbot.messageEmbed

class ReplyCommands : Extension() {
	override val name = "reply-commands"

	override suspend fun setup() {
		ephemeralSlashCommand(::ReplyArgs) {
			name = "reply"
			description = "Reply to the user this thread channel is owned by"

			check {
				inGuild(MAIL_SERVER)
			}
			action {
				val userToDm = inThreadChannel() ?: return@action

				val dmChannel = this@ephemeralSlashCommand.kord.getUser(userToDm)!!.getDmChannel()

				dmChannel.createMessage {
					embed {
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				channel.createMessage {
					embed {
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				respond { content = "Message sent" }
			}
		}

		ephemeralSlashCommand(::ReplyArgs) {
			name = "anonreply"
			description = "Reply anonymously to the user this thread channel is owned by"

			check {
				inGuild(MAIL_SERVER)
			}

			action {
				val userToDm = inThreadChannel() ?: return@action

				val dmChannel = this@ephemeralSlashCommand.kord.getUser(userToDm)!!.getDmChannel()

				dmChannel.createMessage {
					embed {
						// Send an anonymous response to the user
						messageEmbed(arguments.content, user.asUser(), guild!!.id, DISCORD_GREEN, true)
					}
				}

				channel.createMessage {
					embed {
						messageEmbed("(Anonymous) ${arguments.content}", user.asUser(), guild!!.id, DISCORD_GREEN)
					}
				}

				respond { content = "Message sent" }
			}
		}
	}

	inner class ReplyArgs : Arguments() {
		val content by string {
			name = "message"
			description = "What you'd like to reply with"

			mutate {
				it.replace("\\n", "\n").replace("\n", "\n")
			}
		}
	}
}
