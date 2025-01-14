/*
 * Copyright (c) 2022-2025 NoComment1105 <nocomment1105@outlook.com>
 *
 * This file is part of ModMail.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

@file:OptIn(PrivilegedIntent::class)

package io.github.nocomment1105.modmailbot

import dev.kord.cache.map.MapLikeCollection
import dev.kord.cache.map.internal.MapEntryCache
import dev.kord.cache.map.lruLinkedHashMap
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.data.api.DataCollection
import io.github.nocomment1105.modmailbot.extensions.commands.CloseCommands
import io.github.nocomment1105.modmailbot.extensions.commands.ReplyCommands
import io.github.nocomment1105.modmailbot.extensions.events.MessageEditing
import io.github.nocomment1105.modmailbot.extensions.events.MessageReceiving
import modmailbot.i18n.Translations

suspend fun main() {
	val bot = ExtensibleBot(BOT_TOKEN) {
		dataCollectionMode = DataCollection.None

		database(false)

		extensions {
			add(::MessageReceiving)
			add(::ReplyCommands)
			add(::MessageEditing)
			add(::CloseCommands)
		}

		intents {
			+Intent.GuildMembers // Privileged intent
			+Intent.MessageContent // Privileged intent

			+Intent.DirectMessages
			+Intent.GuildMessages
			+Intent.Guilds
			+Intent.GuildVoiceStates
			+Intent.GuildMessageTyping
			+Intent.DirectMessageTyping
		}

		presence {
			watching("for your DMs!")
		}

		kord {
			stackTraceRecovery = true

			cache {
				messages { cache, description ->
					// Set a max message cache size of 1000 messages to avoid creating a crazy large cache
					MapEntryCache(cache, description, MapLikeCollection.lruLinkedHashMap(1000))
				}
			}
		}

		about {
			ephemeral = false
			general {
				message { locale ->
					embed {
						title = Translations.About.embedTitle.translate()

						// TODO A logo that can go here
// 						thumbnail {
// 							url = ""
// 						}

						description = Translations.About.embedDesc.translate()

						field {
							name = Translations.About.howSupportTitle.translate()
							value = Translations.About.howSupportValue.translate()
						}

						field {
							name = Translations.About.version.translate()
							// TODO Install Blossom and do the thing for versions
							value = ""
						}

						field {
							name = Translations.About.usefulLinksName.translate()
							value = Translations.About.usefulLinksValue.translate()
						}
					}

					actionRow {
						// TODO All of this lmao
						linkButton("") {
							label = Translations.About.inviteButton.translate()
						}

						linkButton("") {
							label = Translations.About.privacyButton.translate()
						}

						linkButton("") {
							label = Translations.About.tosButton.translate()
						}
					}
				}
			}
		}

		i18n {
			interactionUserLocaleResolver()
			interactionGuildLocaleResolver()

			applicationCommandLocale(SupportedLocales.ENGLISH)
		}

// TODO Install Doc gen
// 		docGenerator {
//
// 		}
	}

	bot.start()
}
