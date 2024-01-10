@file:Suppress("unused")

package me.blast.command.argument.builder

import me.blast.command.BaseSubcommand
import me.blast.command.argument.Argument
import me.blast.command.argument.InitialArg
import me.blast.command.dsl.SubcommandBuilder
import me.blast.command.text.TextSubcommand

abstract class SubArgumentBuilder(open val guildOnly: Boolean) {
  fun SubcommandBuilder.option(description: String? = null, fullName: String? = null, shortName: String? = null): InitialArg<String> {
    return InitialArg<String>(options).apply {
      argumentType = ArgumentType.OPTION
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  inline fun <reified T : Any> SubcommandBuilder.option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): InitialArg<T> {
    return InitialArg<T>(options).apply {
      argumentType = ArgumentType.OPTION
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
      argumentValidator = validator
      argumentReturnValue = T::class
    }
  }
  
  fun SubcommandBuilder.flag(description: String? = null, fullName: String? = null, shortName: String? = null): Argument<Boolean> {
    return InitialArg<Boolean>(options).apply {
      argumentType = ArgumentType.FLAG
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
      argumentReturnValue = Boolean::class
    }
  }
  
  fun SubcommandBuilder.positional(description: String? = null, fullName: String? = null): InitialArg<String> {
    return InitialArg<String>(options).apply {
      argumentType = ArgumentType.POSITIONAL
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
    }
  }
  
  inline fun <reified T> SubcommandBuilder.positional(description: String? = null, fullName: String? = null, noinline validator: String.() -> T): InitialArg<T> {
    return InitialArg<T>(options).apply {
      argumentType = ArgumentType.POSITIONAL
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentValidator = validator
      argumentReturnValue = T::class
    }
  }
}