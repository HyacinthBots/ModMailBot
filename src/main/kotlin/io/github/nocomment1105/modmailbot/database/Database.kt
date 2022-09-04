/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.github.nocomment1105.modmailbot.MONGO_URI
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
	/** The settings to connect to the database with. */
	private val settings = MongoClientSettings
		.builder()
		.uuidRepresentation(UuidRepresentation.STANDARD)
		.applyConnectionString(ConnectionString(MONGO_URI))
		.build()

	private val client = KMongo.createClient(settings).coroutine

	/** The database. */
	val database get() = client.getDatabase("modmail")

	/**
	 * Runs the migrations for the database.
	 */
	suspend fun migrate() {
		Migrator.migrate()
	}
}
