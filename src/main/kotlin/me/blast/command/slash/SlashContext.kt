package me.blast.command.slash

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.SlashCommandInteraction

data class SlashContext(
  val event: SlashCommandCreateEvent,
  val interaction: SlashCommandInteraction,
  val server: Server?,
  val channel: TextChannel?,
  val user: User
)