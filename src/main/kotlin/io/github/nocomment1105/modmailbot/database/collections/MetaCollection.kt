/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.collections

import dev.kordex.core.koin.KordExKoinComponent
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.eq
import io.github.nocomment1105.modmailbot.database.DatabaseUtils.findOne
import io.github.nocomment1105.modmailbot.database.entities.MetaData
import org.koin.core.component.inject

/**
 * The class stores the functions for interacting with the Meta database.
 */
class MetaCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<MetaData>(name)

	/**
	 * Gets the Meta from the database.
	 *
	 * @return The metadata
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun get(): MetaData? =
		collection.findOne()

	/**
	 * Set's the meta for the first time.
	 *
	 * @param meta The meta to add to the database
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun set(meta: MetaData) =
		collection.insertOne(meta)

	/**
	 * Updates the meta in the database with the [new meta][meta].
	 *
	 * @param meta The new meda data
	 * @author NoComment1105
	 * @since 1.0.0
	 */
	suspend fun update(meta: MetaData) =
		collection.findOneAndReplace(
			eq(MetaData::id, "meta"), meta
		)

	companion object : CollectionBase("metadata")
}
