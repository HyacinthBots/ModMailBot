package io.github.nocomment1105.modmailbot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import io.github.nocomment1105.modmailbot.database.Database
import io.github.nocomment1105.modmailbot.database.entities.CloseQueueData
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * The class for interacting with the CloseQueue database.
 */
class CloseQueueCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.database.getCollection<CloseQueueData>()

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
		collection.deleteOne(CloseQueueData::threadId eq threadId)
}
