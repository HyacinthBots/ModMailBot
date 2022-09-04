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
	@Suppress("MemberNameEqualsClassName")
	val database get() = client.getDatabase("modmail")

	/**
	 * Runs the migrations for the database.
	 */
	suspend fun migrate() {
		Migrator.migrate()
	}
}
