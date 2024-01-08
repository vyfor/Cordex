@file:Suppress("unused")

package me.blast.command.argument.builder

import me.blast.command.argument.Argument
import me.blast.command.argument.InitialArg

abstract class ArgumentBuilder(open val guildOnly: Boolean) {
  val options = ArrayList<Argument<*>>()
  
  fun option(description: String? = null, fullName: String? = null, shortName: String? = null): InitialArg<String> {
    return InitialArg<String>(options).apply {
      argumentType = ArgumentType.OPTION
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  inline fun <reified T : Any> option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): InitialArg<T> {
    return InitialArg<T>(options).apply {
      argumentType = ArgumentType.OPTION
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
      argumentValidator = validator
      argumentReturnValue = T::class
    }
  }
  
  fun flag(description: String? = null, fullName: String? = null, shortName: String? = null): Argument<Boolean> {
    return InitialArg<Boolean>(options).apply {
      argumentType = ArgumentType.FLAG
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
      argumentReturnValue = Boolean::class
    }
  }
  
  fun positional(description: String? = null, fullName: String? = null): InitialArg<String> {
    return InitialArg<String>(options).apply {
      argumentType = ArgumentType.POSITIONAL
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
    }
  }
  
  inline fun <reified T> positional(description: String? = null, fullName: String? = null, noinline validator: String.() -> T): InitialArg<T> {
    return InitialArg<T>(options).apply {
      argumentType = ArgumentType.POSITIONAL
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentValidator = validator
      argumentReturnValue = T::class
    }
  }
}