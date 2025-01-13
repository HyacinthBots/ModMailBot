/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.collections

import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.eq
import io.github.nocomment1105.modmailbot.database.entities.CloseQueueData
import kotlinx.coroutines.flow.toList
import org.koin.core.component.inject

/**
 * The class for interacting with the CloseQueue database.
 */
class CloseQueueCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<CloseQueueData>(name)

	/**
	 * Gets all the threads in the queue.
	 *
	 * @return All the threads in the database as a list
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun getThreadsInQueue(): List<CloseQueueData> = collection.find().toList()

	/**
	 * Adds a thread to the queue.
	 *
	 * @param closeQueueData The data of the thread to queue
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun addThreadToQueue(closeQueueData: CloseQueueData) = collection.insertOne(closeQueueData)

	/**
	 * Removes a thread from the queue.
	 *
	 * @param threadId The ID of the thread to remove
	 *
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend inline fun removeThreadFromQueue(threadId: Snowflake) =
		collection.deleteOne(eq(CloseQueueData::threadId, threadId))

	companion object : CollectionBase("close-queue")
}
