/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.entities.SentMessageData
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * The class for interacting with the SentMessageData database.
 */
class SentMessageCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<SentMessageData>()

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
		collection.find(SentMessageData::threadId eq threadId).toList()

	/**
	 * Gets every message internal sent in a thread.
	 *
	 * @param threadId The ID of the thread to get messages for
	 * @return A list of the internal message data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getInternalSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(SentMessageData::threadId eq threadId, SentMessageData::wasSentByStaff eq true).toList()

	/**
	 * Gets every message sent by an external user in a thread.
	 *
	 * @param threadId The ID of the thread to get messages for
	 * @return A list of the user message data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getUserSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(SentMessageData::threadId eq threadId, SentMessageData::wasSentByStaff eq false).toList()

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
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::messageNumber eq number)

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
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::threadMessageId eq messageId)?.dmMessageId

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
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::dmMessageId eq messageId)?.threadMessageId

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
	} catch (e: NoSuchElementException) {
		1
	}
}
