@file:Suppress("EmptyRange", "unused")

package me.blast.command.argument

import me.blast.command.argument.builder.ArgumentType
import me.blast.utils.Utils
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

sealed class Argument<T>(copyFrom: Argument<*>? = null, val options: ArrayList<Argument<*>>) {
  lateinit var argumentServer: Server
  lateinit var argumentChannel: TextChannel
  lateinit var argumentUser: User
  lateinit var argumentType: ArgumentType
  var argumentName: String? = null
  var argumentShortName: String? = null
  var argumentDescription = "No description provided."
  var argumentValidator: (String.() -> Any?)? = null
    set(value) {
      if (argumentListValidator != null) argumentListValidator = null
      field = value
    }
  var argumentListValidator: (List<String>.() -> Any)? = null
    set(value) {
      if (argumentValidator != null) argumentValidator = null
      field = value
    }
  var argumentIsOptional = false
  var argumentDefaultValue: Any? = null
  var argumentRange: IntRange = 1..1
  var guildOnly = false
  var argumentReturnValue: KClass<*> = String::class
  var argumentChoices: Map<String, String>? = null
  
  init {
    if (copyFrom != null) {
      argumentType = copyFrom.argumentType
      argumentName = copyFrom.argumentName
      argumentShortName = copyFrom.argumentShortName
      argumentDescription = copyFrom.argumentDescription
      argumentValidator = copyFrom.argumentValidator
      argumentListValidator = copyFrom.argumentListValidator
      argumentIsOptional = copyFrom.argumentIsOptional
      argumentDefaultValue = copyFrom.argumentDefaultValue
      argumentRange = copyFrom.argumentRange
      argumentReturnValue = copyFrom.argumentReturnValue
    }
  }
  
  operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
  
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Argument<T> {
    if (argumentName == null) argumentName = Utils.convertCamelToKebab(property.name)
    else require(argumentName!!.isNotBlank())
    if (argumentType != ArgumentType.POSITIONAL) {
      if (argumentShortName == null) argumentShortName = argumentName!!.substring(0, 1)
      else require(argumentShortName!!.isNotBlank())
      if (argumentType == ArgumentType.FLAG) {
        argumentIsOptional = true
        argumentDefaultValue = false
      }
    }
    require(argumentRange.first >= 0 || argumentRange.last >= 0) { "Argument's range must be positive!" }
    if (argumentRange.first == 0) argumentIsOptional = true
    require(options.none { it.argumentName == argumentName || it.argumentShortName == argumentShortName }) { "Argument with name --${argumentName} or -${argumentShortName} already exists!" }
    options.add(this)
    return this
  }
}

interface Base<T>

interface Multiple<T>

interface NonNull<T> : Base<T>

open class InitialArg<T>(options: ArrayList<Argument<*>>) : Argument<T>(options = options), NonNull<T>

open class OptionalArg<T>(copyFrom: Argument<*>?, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options = options), Base<T>

open class MultipleArg<T>(copyFrom: Argument<*>?, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options = options), Multiple<T>, NonNull<T>

open class DefaultArg<T>(copyFrom: Argument<*>?, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options = options), NonNull<T>

open class FinalizedArg<T>(copyFrom: Argument<*>?, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options = options), Multiple<T>, NonNull<T>

/**
 * Converts an argument into an optional argument, enabling nullable values.
 *
 * @return An optional [Argument] with nullable value.
 */
fun <T> InitialArg<T>.optional(): OptionalArg<T?> {
  return OptionalArg<T?>(this, options).apply {
    argumentIsOptional = true
  }
}

/**
 * Converts an argument into an optional argument with a specified default value.
 *
 * @param defaultValue The default value to use when the argument is absent.
 * @return An optional [Argument] with default value.
 */
inline fun <T, reified R : Any> InitialArg<T>.optional(defaultValue: R): DefaultArg<R> {
  return DefaultArg<R>(this, options).apply {
    argumentIsOptional = true
    argumentDefaultValue = defaultValue
    argumentReturnValue = R::class
  }
}

/**
 * Converts an argument into an optional argument with a custom validation function.
 *
 * @param validator A function that validates and processes the argument value.
 * @return An optional [Argument] with nullable value and custom validation.
 */
inline fun <T, reified R : Any> InitialArg<T>.optional(noinline validator: String.() -> R): OptionalArg<R?> {
  return OptionalArg<R?>(this, options).apply {
    argumentIsOptional = true
    argumentValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Converts an argument into an optional argument with a default value and custom validation.
 *
 * @param default The default value to use when the argument is absent.
 * @param validator A function that validates and processes the argument value.
 * @return An optional [Argument] with default value and custom validation.
 */
inline fun <T, reified R : Any> InitialArg<T>.optional(default: R, noinline validator: String.() -> R): DefaultArg<R> {
  return DefaultArg<R>(this, options).apply {
    argumentIsOptional = true
    argumentDefaultValue = default
    argumentValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Converts a multi-value argument into an optional argument.
 *
 * @return An optional [Argument] with multiple values.
 */
fun <T> MultipleArg<List<T>>.optional(): FinalizedArg<List<T>> {
  return FinalizedArg<List<T>>(this, options).apply {
    argumentIsOptional = true
  }
}

/**
 * Converts a multi-value argument into an optional argument with a default value.
 *
 * @param defaultValues An array of default values to use when the argument is absent.
 * @return An optional [Argument] with a default value that accepts multiple values.
 */
inline fun <T, reified R : Any> MultipleArg<List<T>>.optional(vararg defaultValues: R): FinalizedArg<List<R>> {
  return FinalizedArg<List<R>>(this, options).apply {
    argumentIsOptional = true
    argumentDefaultValue = listOf(*defaultValues)
    argumentReturnValue = R::class
  }
}

/**
 * Converts a multi-value argument into an optional argument with custom list validation.
 *
 * @param validator A function that validates and processes the list of argument values.
 * @return An optional [Argument] with custom validation.
 */
inline fun <T, reified R : Any> MultipleArg<List<T>>.optional(noinline validator: List<String>.() -> R): FinalizedArg<R> {
  return FinalizedArg<R>(this, options).apply {
    argumentIsOptional = true
    argumentListValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Converts a multi-value argument into an optional argument with a default value and custom list validation.
 *
 * @param defaultValues An array of default values to use when the argument is absent.
 * @param validator A function that validates and processes the list of argument values.
 * @return An optional [Argument] with a default value and custom validation.
 */
inline fun <T, reified R : Any> MultipleArg<List<T>>.optional(vararg defaultValues: R, noinline validator: List<String>.() -> R): FinalizedArg<R> {
  return FinalizedArg<R>(this, options).apply {
    argumentIsOptional = true
    argumentDefaultValue = listOf(*defaultValues)
    argumentListValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Modifies an argument to support accepting multiple values within a specified range.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @return An [Argument] with multiple values.
 */
fun <T> InitialArg<T>.multiple(range: IntRange = 1..0): MultipleArg<List<T>> {
  return MultipleArg<List<T>>(this, options).apply {
    argumentRange = range
  }
}

/**
 * Modifies an argument to support accepting multiple values within a specified range and with custom validation.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @param validator A function that validates and processes the list of argument values.
 * @return An [Argument] with multiple values and custom validation.
 */
inline fun <T, reified R : Any> InitialArg<T>.multiple(range: IntRange = 1..0, noinline validator: List<String>.() -> R): MultipleArg<R> {
  return MultipleArg<R>(this, options).apply {
    argumentRange = range
    argumentListValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Modifies an optional argument to support accepting multiple values within a specified range.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @return An optional [Argument] with multiple values.
 */
fun <T> OptionalArg<T?>.multiple(range: IntRange = 1..0): FinalizedArg<List<T>> {
  return FinalizedArg<List<T>>(this, options).apply {
    argumentRange = range
  }
}

/**
 * Modifies an optional argument to support accepting multiple values within a specified range and with custom validation.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @param validator A function that validates and processes the list of argument values.
 * @return An optional [Argument] with multiple and custom validation.
 */
inline fun <T, reified R : Any> OptionalArg<T?>.multiple(range: IntRange = 1..0, noinline validator: List<String>.() -> R): FinalizedArg<R> {
  return FinalizedArg<R>(this, options).apply {
    argumentRange = range
    argumentListValidator = validator
    argumentReturnValue = R::class
  }
}

/**
 * Modifies an argument with a default value to support accepting multiple values within a specified range.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @return An optional [Argument] with multiple values.
 */
fun <T> DefaultArg<T>.multiple(range: IntRange = 1..0): MultipleArg<List<T>> {
  return MultipleArg<List<T>>(this, options).apply {
    argumentRange = range
  }
}

/**
 * Modifies an argument with a default value to support accepting multiple values within a specified range and with custom validation.
 *
 * @param range The allowable range of values for the multiple argument.
 *
 * Start value set to `0` makes the argument optional.
 *
 * End value set to `0` lets the argument accept unlimited amount of values.
 * @param validator A function that validates and processes the list of argument values.
 * @return An optional [Argument] with a default value that accepts multiple values and custom validation.
 */
inline fun <T, reified R : Any> DefaultArg<T>.multiple(range: IntRange = 1..0, noinline validator: List<String>.() -> R): MultipleArg<R> {
  return MultipleArg<R>(this, options).apply {
    argumentRange = range
    argumentListValidator = validator
    argumentReturnValue = R::class
  }
}