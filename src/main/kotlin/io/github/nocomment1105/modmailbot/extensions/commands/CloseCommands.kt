package io.github.nocomment1105.modmailbot.extensions.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingOptionalDuration
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.create.embed
import io.github.nocomment1105.modmailbot.MAIL_SERVER
import io.github.nocomment1105.modmailbot.database.collections.CloseQueueCollection
import io.github.nocomment1105.modmailbot.database.collections.OpenThreadCollection
import io.github.nocomment1105.modmailbot.database.collections.SentMessageCollection
import io.github.nocomment1105.modmailbot.database.entities.CloseQueueData
import io.github.nocomment1105.modmailbot.inThreadChannel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

class CloseCommands : Extension() {
	override val name = "close-commands"

	/** The scheduler to run the task on a schedule for. */
	private val scheduler = Scheduler()

	/** The task to attach the [scheduler] too. */
	private lateinit var task: Task

	override suspend fun setup() {
		task = scheduler.schedule(30, pollingSeconds = 1, repeat = true, callback = ::closeThreads)

		ephemeralSlashCommand(::CloseArgs) {
			name = "close"
			description = "Close this thread"

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
			name = "delay"
			description = "How long until you want to close this thread"
		}

		val silent by defaultingBoolean {
			name = "silent"
			description = "Whether to close this thread silently"
			defaultValue = false
		}

		val message by optionalString {
			name = "message"
			description = "The closing message to send to the user."
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
			title = "Modmail thread closed"
			if (closeMessage != null) description = closeMessage
			footer {
				text = "Replying will create a new thread"
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

	SentMessageCollection().removeMessages(channel.id)
	OpenThreadCollection().removeByThread(channel.id)
	CloseQueueCollection().removeThreadFromQueue(channel.id)
}
