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
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.findOne
import io.github.nocomment1105.modmailbot.database.entities.OpenThreadData
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject

/**
 * This class stores the functions for interacting with the open thread database.
 */
class OpenThreadsCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<OpenThreadData>(name)

	/**
	 * Adds a thread to the database.
	 * @param openThreadData The data to add to the database
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun add(openThreadData: OpenThreadData) = collection.insertOne(openThreadData)

	/**
	 * Removes a thread from the database.
	 *
	 * @param userId The users' thread to remove
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun removeByUser(userId: Snowflake) = collection.deleteOne(eq(OpenThreadData::userId, userId))

	/**
	 * Removes a thread from the database.
	 *
	 * @param threadId The thread id to remove
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun removeByThread(threadId: Snowflake) = collection.deleteOne(eq(OpenThreadData::threadId, threadId))

	/**
	 * Gets the open thread for the user. There should only ever be one at a time, hence we return the [first] element
	 * in the list of threads.
	 * @param userId The users' thread to get
	 * @return The first thread we come across as there should only ever be one
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun getOpenThreadsForUser(userId: Snowflake): OpenThreadData? =
		collection.find(eq(OpenThreadData::userId, userId)).first()

	/**
	 * Gets the DM id from the [threadId].
	 *
	 * @param threadId The channel to get the DM id from
	 * @return The user ID, also the dm channel id
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun getDmFromThreadChannel(threadId: Snowflake): Snowflake? =
		collection.findOne(eq(OpenThreadData::threadId, threadId))?.userId

	companion object : CollectionBase("open-threads")
}
