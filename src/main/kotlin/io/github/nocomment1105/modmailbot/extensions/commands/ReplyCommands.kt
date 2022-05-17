package io.github.nocomment1105.modmailbot.extensions.commands

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.checks.inGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.DatabaseManager
import io.github.nocomment1105.modmailbot.database.getDmFromThreadChannel
import io.github.nocomment1105.modmailbot.messageEmbed
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ReplyCommands : Extension() {

	override val name = "replycommands"

	override suspend fun setup() {
		ephemeralSlashCommand(::ReplyArgs) {
			name = "reply"
			description = "Reply to the user this thread channel is owned by"

			check {
				inGuild(MAIL_SERVER)
			}
			action {
				var userToDm: String? = null
				newSuspendedTransaction {
					userToDm = try {
						getDmFromThreadChannel(channel.id, DatabaseManager.OpenThreads.userId)
					} catch (e: NoSuchElementException) {
						respond {
							content = "**Error**: This channel does not belong to a user! Use this command in user " +
									"channels only"
						}
						null
					}
				}
				if (userToDm == null) return@action

				val dmChannel = this@ephemeralSlashCommand.kord.getUser(Snowflake(userToDm!!))!!.getDmChannel()

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

		// TODO Anonymous replies
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
