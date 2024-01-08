package me.blast.core

import me.blast.parser.exception.ArgumentException
import me.blast.utils.command.suggestions.DistanceAccuracy
import me.blast.utils.cooldown.Cooldown
import me.blast.utils.cooldown.CooldownType
import me.blast.utils.event.CommandEvent
import me.blast.utils.event.TextCommandEvent

class CordexConfiguration {
  var prefix: (Long) -> String = { "!" }
  var errorHandler: ((CommandEvent, Exception) -> Unit)? = null
  var parsingErrorHandler: ((CommandEvent, ArgumentException) -> Unit)? = null
  var cooldownHandler: ((CommandEvent, Cooldown, CooldownType) -> Unit)? = null
  var enableCommandSuggestions: Boolean = false
  var commandSuggestionAccuracy: DistanceAccuracy = DistanceAccuracy.STRICT
  val interceptors: MutableMap<String, (TextCommandEvent) -> Boolean> = mutableMapOf()
}