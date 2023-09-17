package me.blast.core

import me.blast.command.Command
import me.blast.parser.exception.ArgumentException
import me.blast.utils.command.suggestions.DistanceAccuracy
import me.blast.utils.cooldown.Cooldown
import me.blast.utils.cooldown.CooldownType
import org.javacord.api.event.message.MessageCreateEvent

class CordexConfiguration {
  var prefix: (Long) -> String = { "!" }
  var errorHandler: ((MessageCreateEvent, Command, Exception) -> Unit)? = null
  var parsingErrorHandler: ((MessageCreateEvent, Command, ArgumentException) -> Unit)? = null
  var cooldownHandler: ((MessageCreateEvent, Command, Cooldown, CooldownType) -> Unit)? = null
  var enableCommandSuggestions: Boolean = false
  var commandSuggestionAccuracy: DistanceAccuracy = DistanceAccuracy.STRICT
}