package me.blast.utils.event

import me.blast.command.text.TextCommand
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import kotlin.jvm.optionals.getOrNull

class TextCommandEvent(override val event: MessageCreateEvent, override val command: TextCommand) : CommandEvent(event, command) {
  override val server: Server?
    get() = event.server.getOrNull()
  override val channel: TextChannel
    get() = event.channel
  override val user: User
    get() = event.messageAuthor.asUser().get()
}