package me.blast.utils.extensions

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.Interaction
import org.javacord.api.interaction.InteractionBase

fun InteractionBase.respondEphemerally(content: String) {
  createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent(content).respond()
}

fun InteractionBase.respondEphemerally(embed: EmbedBuilder) {
  createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).addEmbed(embed).respond()
}