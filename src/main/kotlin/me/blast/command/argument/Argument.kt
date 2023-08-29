package me.blast.command.argument

import me.blast.command.argument.builder.ArgumentType
import me.blast.utils.Utils
import org.javacord.api.event.message.MessageCreateEvent
import kotlin.reflect.KProperty

open class Argument<T>(copyFrom: Argument<*>? = null, val options: ArrayList<Argument<*>>) {
  lateinit var argumentEvent: MessageCreateEvent
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
  
  // Setting first range value to 0 will make the argument optional
  // Setting last range value to 0 will make the argument take infinite amount of values
  var argumentRange: IntRange = 1..1
  var guildOnly = false
  
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
      guildOnly = copyFrom.guildOnly
    }
  }
  
  operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
  
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Argument<T> {
    if (argumentName == null) argumentName = Utils.convertCamelToKebab(property.name) else require(argumentName!!.isNotBlank())
    if (argumentShortName == null && argumentType != ArgumentType.POSITIONAL) argumentShortName = argumentName!!.substring(0, 1) else require(argumentShortName!!.isNotBlank())
    require(argumentRange.first >= 0 || argumentRange.last >= 0) { "Argument's range must be positive!" }
    if (argumentRange.first == 0) argumentIsOptional = true
    require(options.none { it.argumentName == argumentName || it.argumentShortName == argumentShortName }) { "Argument with name --${argumentName} or -${argumentShortName} already exists!" }
    options.add(this)
    return this
  }
}

open class MultiValueArgument<T>(copyFrom: Argument<*>? = null, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options)

open class OptionalArgument<T>(copyFrom: Argument<*>? = null, options: ArrayList<Argument<*>>) : Argument<T>(copyFrom, options)

open class OptionalMultiValueArgument<T>(copyFrom: Argument<*>? = null, options: ArrayList<Argument<*>>) : MultiValueArgument<T>(copyFrom, options)