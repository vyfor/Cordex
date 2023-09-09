package me.blast.parser.exception

import me.blast.command.argument.Argument

sealed class ArgumentException(override val message: String?) : Exception(message) {
  /**
   * Represents an exception where an argument's value is empty.
   *
   * @property argument The argument with an empty value.
   */
  data class Empty(val argument: Argument<*>, override val message: String? = null) : ArgumentException(message)
  
  /**
   * Represents an exception where an invalid value is provided for an argument.
   *
   * @property argument The argument with an invalid value.
   * @property input The input provided to the argument which failed the validation.
   */
  data class Invalid(val argument: Argument<*>, val input: String, override val message: String? = null) : ArgumentException(message)
  
  /**
   * Represents an exception where there is an insufficient amount of values provided for an argument.
   *
   * @property argument The argument for which there is insufficient values input.
   * @property input The values input that is insufficient for the argument.
   */
  data class Insufficient(val argument: Argument<*>, val input: String, override val message: String? = null) : ArgumentException(message)
  
  /**
   * Represents an exception where one or more required arguments are missing.
   *
   * @property arguments The list of missing arguments.
   */
  data class Missing(val arguments: List<Argument<*>>, override val message: String? = null) : ArgumentException(message)
}