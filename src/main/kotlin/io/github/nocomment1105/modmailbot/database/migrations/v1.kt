/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.migrations

import org.litote.kmongo.coroutine.CoroutineDatabase

@Suppress("UnusedPrivateMember")
suspend fun v1(db: CoroutineDatabase) {
	// Currently no migration needed. This is in preparation for actually needing one
}
