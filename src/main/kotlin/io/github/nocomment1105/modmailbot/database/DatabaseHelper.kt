package io.github.nocomment1105.modmailbot.database

import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Using the provided [userId] and [column] a value will be looked for and returned from the database. If it does not
 * exist a [NoSuchElementException] is thrown by the function
 *
 * @param userId The ID of the user who has a thread
 * @param column The column you want the information form
 * @return The requested [String] from the database
 * @throws NoSuchElementException when the values cannot be found
 * @author NoComment1105
 * @since 1.0.0
 */
suspend fun getOpenThreadsForUser(userId: Snowflake, column: Column<String>) = newSuspendedTransaction {
	DatabaseManager.OpenThreads.select {
		DatabaseManager.OpenThreads.userId eq userId.toString()
	}.single()[column]
}
