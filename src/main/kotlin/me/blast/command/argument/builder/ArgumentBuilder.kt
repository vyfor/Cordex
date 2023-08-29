@file:Suppress("unused")

package me.blast.command.argument.builder

import me.blast.command.argument.Argument
import me.blast.command.argument.FlagArgument
import me.blast.command.argument.OptionArgument
import me.blast.command.argument.PositionalArgument

abstract class ArgumentBuilder(open val guildOnly: Boolean) {
  val options = ArrayList<Argument<*>>()
  
  fun option(description: String? = null, fullName: String? = null, shortName: String? = null): OptionArgument<String> {
    return OptionArgument<String>(options).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  inline fun <reified T> option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): OptionArgument<T> {
    return OptionArgument<T>(options).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
      argumentValidator = validator
    }
  }
  
  fun flag(description: String? = null, fullName: String? = null, shortName: String? = null): Argument<Boolean> {
    return FlagArgument(options).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  fun positional(description: String? = null, fullName: String? = null): PositionalArgument<String> {
    return PositionalArgument<String>(options).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
    }
  }
  
  inline fun <reified T> positional(description: String? = null, fullName: String? = null, noinline validator: String.() -> T): PositionalArgument<T> {
    return PositionalArgument<T>(options).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentValidator = validator
    }
  }
}