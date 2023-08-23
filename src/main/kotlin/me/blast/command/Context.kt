package me.blast.command

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent

data class Context(
  val event: MessageCreateEvent,
  val server: Server,
  val channel: TextChannel,
  val user: User,
  val message: Message,
  val prefix: String,
  val args: Array<String>
)
