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

class SentMessageCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<SentMessageData>()

	suspend inline fun addMessage(sendMessageData: SentMessageData) =
		collection.insertOne(sendMessageData)

	suspend inline fun getMessagesInThread(threadId: Snowflake): List<SentMessageData?> =
		collection.find(SentMessageData::threadId eq threadId).toList()

	suspend inline fun getInternalSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(SentMessageData::threadId eq threadId, SentMessageData::wasSentByStaff eq true).toList()

	suspend inline fun getUserSentMessages(threadId: Snowflake): List<SentMessageData?> =
		collection.find(SentMessageData::threadId eq threadId, SentMessageData::wasSentByStaff eq false).toList()

	suspend inline fun getMessageByNumber(threadId: Snowflake, number: Int): SentMessageData? =
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::messageNumber eq number)

	suspend inline fun getDmMessageById(threadId: Snowflake, messageId: Snowflake): Snowflake? =
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::threadMessageId eq messageId)?.dmMessageId

	suspend inline fun getInternalMessageById(threadId: Snowflake, messageId: Snowflake): Snowflake? =
		collection.findOne(SentMessageData::threadId eq threadId, SentMessageData::dmMessageId eq messageId)?.threadMessageId

	suspend inline fun getNextMessageNumber(threadId: Snowflake): Int = try {
		getMessagesInThread(threadId).last()!!.messageNumber + 1
	} catch (e: NoSuchElementException) {
		1
	}
}
