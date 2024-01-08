@file:Suppress("unused")

package me.blast.utils.event

import me.blast.command.BaseCommand
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.Event

sealed class CommandEvent(open val event: Event, open val command: BaseCommand) {
  abstract val server: Server?
  abstract val channel: TextChannel?
  abstract val user: User
  
  fun toTextCommand() = this as? TextCommandEvent
  fun toSlashCommand() = this as? SlashCommandEvent
}