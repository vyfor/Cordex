package me.blast.core

import me.blast.command.Command
import me.blast.parser.exception.ArgumentException
import org.javacord.api.event.message.MessageCreateEvent

class CordexConfiguration {
  var prefix: (Long) -> String = { "!" }
  var errorHandler: ((MessageCreateEvent, Command, Exception) -> Unit)? = null
  var parsingErrorHandler: ((MessageCreateEvent, Command, ArgumentException) -> Unit)? = null
}