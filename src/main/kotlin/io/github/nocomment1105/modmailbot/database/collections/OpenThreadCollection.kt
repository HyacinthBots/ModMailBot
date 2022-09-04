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
import io.github.nocomment1105.modmailbot.database.entities.OpenThreadData
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * This class stores the functions for interacting with the open thread database.
 */
class OpenThreadCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<OpenThreadData>()

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
	suspend fun remove(userId: Snowflake) = collection.deleteOne(OpenThreadData::userId eq userId)

	/**
	 * Gets the open thread for the user. There should only ever be one at a time, hence we return the [first] element
	 * in the list of threads.
	 * @param userId The users' thread to get
	 * @return The first thread we come across as there should only ever be one
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun getOpenThreadsForUser(userId: Snowflake): OpenThreadData? =
		collection.find(OpenThreadData::userId eq userId).first()

	/**
	 * Gets the DM id from the [threadId].
	 *
	 * @param threadId The channel to get the DM id from
	 * @return The data surrounding the thread
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun getDmFromThreadChannel(threadId: Snowflake): OpenThreadData? =
		collection.findOne(OpenThreadData::threadId eq threadId)
}
