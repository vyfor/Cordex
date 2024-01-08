package me.blast.utils.event

import me.blast.command.slash.SlashCommand
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import kotlin.jvm.optionals.getOrNull

class SlashCommandEvent(override val event: SlashCommandCreateEvent, override val command: SlashCommand) : CommandEvent(event, command) {
  override val server: Server?
    get() = event.slashCommandInteraction.server.getOrNull()
  override val channel: TextChannel?
    get() = event.slashCommandInteraction.channel.getOrNull()
  override val user: User
    get() = event.slashCommandInteraction.user
}