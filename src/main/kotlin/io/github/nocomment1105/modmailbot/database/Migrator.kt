/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import io.github.nocomment1105.modmailbot.database.collections.MetaCollection
import io.github.nocomment1105.modmailbot.database.entities.MetaData
import io.github.nocomment1105.modmailbot.database.migrations.v1
import mu.KotlinLogging
import org.koin.core.component.inject

object Migrator : KordExKoinComponent {
	private val logger = KotlinLogging.logger("Migrator Logger")

	private val db: Database by inject()
	private val mainMetaCollection: MetaCollection by inject()

	suspend fun migrate() {
		logger.info { "Starting main database migration" }

		var meta = mainMetaCollection.get()

		if (meta == null) {
			meta = MetaData(0)

			mainMetaCollection.set(meta)
		}

		var currentVersion = meta.version

		logger.info { "Current main database version: v$currentVersion" }

		while (true) {
			val nextVersion = currentVersion + 1

			@Suppress("TooGenericExceptionCaught")
			try {
				@Suppress("UseIfInsteadOfWhen")
				when (nextVersion) {
					1 -> ::v1
					else -> break
				}(db.database)

				logger.info { "Migrated main database to version $nextVersion." }
			} catch (t: Throwable) {
				logger.error(t) { "Failed to migrate main database to version $nextVersion." }

				throw t
			}

			currentVersion = nextVersion
		}

		if (currentVersion != meta.version) {
			meta = meta.copy(version = currentVersion)

			mainMetaCollection.update(meta)

			logger.info { "Finished main database migrations." }
		}
	}
}
