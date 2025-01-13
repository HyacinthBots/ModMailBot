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
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.embed
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.coalescingOptionalDuration
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.collections.CloseQueueCollection
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadsCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessagesCollection
import io.github.nocomment1105.modmailbot.database.entities.CloseQueueData
import io.github.nocomment1105.modmailbot.inThreadChannel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import modmailbot.i18n.Translations

class CloseCommands : Extension() {
	override val name = "close-commands"

	/** The scheduler to run the task on a schedule for. */
	private val scheduler = Scheduler()

	/** The task to attach the [scheduler] too. */
	private lateinit var task: Task

	override suspend fun setup() {
		task = scheduler.schedule(30, pollingSeconds = 1, repeat = true, callback = ::closeThreads)

		ephemeralSlashCommand(::CloseArgs) {
			name = Translations.Commands.Close.name
			description = Translations.Commands.Close.description

			guild(MAIL_SERVER)

			action {
				val userToDm = inThreadChannel() ?: return@action

				val dmChannel = event.kord.getUser(userToDm)!!.getDmChannel()

				if (arguments.delay != null) {
					val closeTime = Clock.System.now().plus(arguments.delay!!, TimeZone.UTC)

					CloseQueueCollection().addThreadToQueue(
						CloseQueueData(
							channel.id,
							userToDm,
							closeTime,
							arguments.silent,
							arguments.message
						)
					)

					return@action
				} else {
					if (!arguments.silent) {
						sendThreadCloseMessage(dmChannel, channel.asChannel(), arguments.message)
					} else {
						deleteThread(channel.asChannel(), arguments.message)
					}
				}
			}
		}
	}

	inner class CloseArgs : Arguments() {
		val delay by coalescingOptionalDuration {
			name = Translations.Commands.Close.Args.Delay.name
			description = Translations.Commands.Close.Args.Delay.description
		}

		val silent by defaultingBoolean {
			name = Translations.Commands.Close.Args.Silent.name
			description = Translations.Commands.Close.Args.Silent.description
			defaultValue = false
		}

		val message by optionalString {
			name = Translations.Commands.Close.Args.Message.name
			description = Translations.Commands.Close.Args.Message.description
		}
	}

	/**
	 * Checks the close queue for any threads that need closing and closes them.
	 *
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	private suspend fun closeThreads() {
		val queue = CloseQueueCollection().getThreadsInQueue()

		val now = Clock.System.now().toEpochMilliseconds()

		queue.forEach {
			if (it.timeToClose.toEpochMilliseconds() - now <= 0 && !it.silently) {
				sendThreadCloseMessage(
					kord.getUser(it.userId)!!.getDmChannel(),
					kord.getChannelOf(it.threadId)!!,
					it.message
				)
			} else if (it.timeToClose.toEpochMilliseconds() - now <= 0 && it.silently) {
				deleteThread(kord.getChannelOf(it.threadId)!!, it.message)
			}
		}
	}
}

/**
 * Sends a DM to the user with the close message, if provided and calls [deleteThread].
 *
 * @param dmChannel The channel to send the close message in
 * @param channel The thread channel to close
 * @param closeMessage The message to attach to the close, or none
 *
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun sendThreadCloseMessage(dmChannel: DmChannel, channel: MessageChannel, closeMessage: String?) {
	dmChannel.createMessage {
		embed {
			title = Translations.Commands.Close.ClosedEmbed.title.translate()
			if (closeMessage != null) description = closeMessage
			footer {
				text = Translations.Commands.Close.ClosedEmbed.footer.translate()
			}
			timestamp = Clock.System.now()
		}
	}

	deleteThread(channel, closeMessage)
}

/**
 * Deletes the thread channel, creates a log for moderators to review at another time and removes the thread from the
 * database.
 *
 * @param channel The thread channel to close
 * @param closeMessage The message to attach to the close, or none
 *
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun deleteThread(channel: MessageChannel, closeMessage: String?) {
	channel.delete(closeMessage ?: "Silently closed thread")

	// TODO create a log in some way for moderators to look back on

	SentMessagesCollection().removeMessages(channel.id)
	OpenThreadsCollection().removeByThread(channel.id)
	CloseQueueCollection().removeThreadFromQueue(channel.id)
}
