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

	suspend inline fun getNextMessageNumber(threadId: Snowflake): Int = try {
		getMessagesInThread(threadId).last()!!.messageNumber + 1
	} catch (e: NoSuchElementException) {
		1
	}
}
