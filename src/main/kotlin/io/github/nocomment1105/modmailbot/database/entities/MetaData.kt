/*
 * Copyright (c) 2022 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package io.github.nocomment1105.modmailbot.database.entities

import kotlinx.serialization.Serializable

/**
 * The metadata for the database.
 *
 * @property version The current Database version
 * @property id The table ID. This will never change
 *
 * @since 1.0.0
 */
@Serializable
data class MetaData(
	val version: Int,
	val id: String = "meta"
)
