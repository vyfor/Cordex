@file:Suppress("unused")

package me.blast.utils.extensions

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.InteractionBase
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder

fun InteractionBase.respondEphemerally(content: String) {
  createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent(content).respond()
}

fun InteractionBase.respondEphemerally(vararg embeds: EmbedBuilder) {
  createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).addEmbeds(*embeds).respond()
}

fun InteractionBase.respondEphemerally(block: InteractionImmediateResponseBuilder.() -> Unit) {
  createImmediateResponder().apply(block).setFlags(MessageFlag.EPHEMERAL).respond()
}