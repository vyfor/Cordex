package me.blast.parser.exceptions

import me.blast.command.CommandImpl.Delegate

sealed class ArgumentException : Exception() {
  
  /**
   * Represents an exception where an invalid value is provided for an argument.
   *
   * @property argument The argument with an invalid value.
   */
  data class Invalid(val argument: Delegate<*>) : ArgumentException()
  
  /**
   * Represents an exception where an argument's value is empty.
   *
   * @property argument The argument with an empty value.
   */
  data class Empty(val argument: Delegate<*>) : ArgumentException()
  
  /**
   * Represents an exception where one or more required arguments are missing.
   *
   * @property arguments The list of missing arguments.
   */
  data class Missing(val arguments: List<Delegate<*>>) : ArgumentException()
}