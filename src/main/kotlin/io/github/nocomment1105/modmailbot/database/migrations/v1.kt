/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.migrations

import com.mongodb.kotlin.client.coroutine.MongoDatabase

@Suppress("UnusedPrivateMember")
suspend fun v1(db: MongoDatabase) {
	// Currently no migration needed. This is in preparation for actually needing one
}
