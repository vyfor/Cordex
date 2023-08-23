package me.blast.parser.exceptions

import me.blast.command.CommandImpl.Delegate

sealed class ArgumentException: Exception() {
  data class Invalid(val argument: Delegate<*>): ArgumentException()
  data class Undefined(val argument: Delegate<*>): ArgumentException()
  data class Empty(val argument: Delegate<*>): ArgumentException()
  data class Missing(val arguments: List<Delegate<*>>): ArgumentException()
}