/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.collections

import com.mongodb.client.model.Filters.and
import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.eq
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.findOne
import io.github.nocomment1105.modmailbot.database.entities.SentMessageData
import kotlinx.coroutines.flow.toList
import org.koin.core.component.inject

/**
 * The class for interacting with the SentMessageData database.
 */
class SentMessagesCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<SentMessageData>(name)

	/**
	 * Adds a message to the database.
	 *
	 * @param sendMessageData The message data to add
	 *
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun addMessage(sendMessageData: SentMessageData) =
		collection.insertOne(sendMessageData)

	/**
	 * Gets every message sent in a thread.
	 *
	 * @param threadId The ID of the thread to get messages for
	 * @return A list of the message data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getMessagesInThread(threadId: Snowflake): List<SentMessageData?> =
		collection.find(eq(SentMessageData::threadId, threadId)).toList()

	/**
	 * Gets every message internal sent in a thread.
	 *
	 * @param threadId The ID of the thread to get messages for
	 * @return A list of the internal message data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getInternalSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(and(eq(SentMessageData::threadId, threadId), eq(SentMessageData::wasSentByStaff, true)))
			.toList()

	/**
	 * Gets every message sent by an external user in a thread.
	 *
	 * @param threadId The ID of the thread to get messages for
	 * @return A list of the user message data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getUserSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(and(eq(SentMessageData::threadId, threadId), eq(SentMessageData::wasSentByStaff, false)))
			.toList()

	/**
	 * Gets a message sent by in a thread by its number identifier.
	 *
	 * @param threadId The ID of the thread to get message from
	 * @param number The number identifier of the message
	 * @return The message data of the message
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getMessageByNumber(threadId: Snowflake, number: Int): SentMessageData? =
		collection.findOne(and(eq(SentMessageData::threadId, threadId), eq(SentMessageData::messageNumber, number)))

	/**
	 * Gets a DM message by the ID of its linked thread message. Primarily for use when going from thread message to dm
	 * message.
	 *
	 * @param threadId The ID of the thread to get the message from
	 * @param messageId The ID of the message sent in the internal thread channel
	 * @return The ID of the message in the DM channel
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getDmMessageById(threadId: Snowflake, messageId: Snowflake): Snowflake? =
		collection.findOne(
			and(
				eq(SentMessageData::threadId, threadId),
				eq(SentMessageData::threadMessageId, messageId)
			)
		)?.dmMessageId

	/**
	 * Gets a thread message by the ID of its linked DM message. Primarily for use when going from dm message to thread
	 * message.
	 *
	 * @param threadId The ID of the thread to get the message from
	 * @param messageId The ID of the message sent in the dm channel
	 * @return The ID of the message in the thread channel
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getInternalMessageById(threadId: Snowflake, messageId: Snowflake): Snowflake? =
		collection.findOne(
			and(
				eq(SentMessageData::threadId, threadId),
				eq(SentMessageData::dmMessageId, messageId)
			)
		)?.threadMessageId

	/**
	 * Gets the next number id for sent messages.
	 *
	 * @param threadId The ID of the thread
	 * @return The number id of the message
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getNextMessageNumber(threadId: Snowflake): Int = try {
		getMessagesInThread(threadId).last()!!.messageNumber + 1
	} catch (_: NoSuchElementException) {
		1
	}

	/**
	 * Removes all messages from the database.
	 *
	 * @param threadId THe ID of the thread to remove messages for
	 *
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun removeMessages(threadId: Snowflake) =
		collection.deleteMany(eq(SentMessageData::threadId, threadId))

	companion object : CollectionBase("sent-messages")
}
