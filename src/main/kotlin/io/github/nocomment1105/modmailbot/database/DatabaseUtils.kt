/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.bson.BsonDocument
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

/**
 * This object contains lots of useful functions that keep the collections looking cleaner and mimics some old KMongo
 * functionality, to keep me happy and my code looking nicer.
 */
object DatabaseUtils {
	/**
	 * This function mimics the old functionality of the KMongo infix function without being infix. It also keeps the
	 * collections clean as there aren't lots of `.name` calls in them.
	 *
	 * @param field The database table field
	 * @param value The value to compare against
	 * @return A [Bson] object of the comparison
	 */
	fun <T> eq(field: KProperty<T?>, value: T?): Bson = Filters.eq(field.name, value)

	/**
	 * This extension function gets rid of the need to have lots of `firstOrNull()` calls in the equation and keeps it
	 * similar to the old KMongo function.
	 *
	 * @param filter The [BsonDocument] to filter the find
	 * @return The collection [T]
	 */
	suspend fun <T : Any> MongoCollection<T>.findOne(filter: Bson = BsonDocument()): T? = find(filter).firstOrNull()
}
