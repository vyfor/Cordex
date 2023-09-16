package me.blast.utils.command

import me.blast.command.Command
import me.blast.command.argument.Argument
import me.blast.command.argument.builder.ArgumentType
import me.blast.parser.exception.ArgumentException
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color
import java.util.*
import kotlin.math.abs

object CommandUtils {
  fun Command.generateHelpMessage(user: MessageAuthor) = EmbedBuilder().apply {
    setTitle("Help for ${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} command")
    if (aliases?.isNotEmpty() == true) addField("Aliases", aliases.joinToString(prefix = "`", separator = "`, `", postfix = "`"))
    addField("Description", description)
    setFooter(user.name, user.avatar)
    setTimestampToNow()
    setColor(Color.RED)
    options.takeIf { it.isNotEmpty() }?.let { addField("Arguments", generateArgumentUsage(it)) }
  }
  
  fun generateArgumentUsage(options: List<Argument<*>>, errorMessage: String? = null): String? {
    return options.takeIf { it.isNotEmpty() }?.run {
      val formattedArgs: String
      val formattedOptions: String
      partition { it.argumentType == ArgumentType.POSITIONAL }.apply {
        formattedArgs = first.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
        }
        formattedOptions = second.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
        }
      }
      "${if (errorMessage != null) "> ${errorMessage}\n" else ""}```ansi\n${
        if (formattedArgs.isEmpty()) {
          formattedOptions
        } else if (formattedOptions.isEmpty()) {
          formattedArgs
        } else {
          "\u001B[1;37mPositional Arguments\n$formattedArgs\n\n\u001B[1;37mOptions\n$formattedOptions"
        }
      }```"
    }
  }
  
  fun generateArgumentError(exception: ArgumentException): String {
    return when (exception) {
      is ArgumentException.Empty -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Insufficient -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Invalid -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Missing -> {
        exception.arguments.partition { it.argumentType == ArgumentType.POSITIONAL }.run {
          first.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
          } + second.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
          }
        }
      }
    }
  }
  
  fun findClosestCommands(input: String, commands: Set<String>, maxDistance: Int): Set<String> {
    val result = mutableSetOf<String>()
    
    for (command in commands) {
      if (abs(input.length - command.length) > maxDistance) continue
      val distances = IntArray(input.length + 1) { it }
      for (i in 1..command.length) {
        var prev = i
        for (j in 1..input.length) {
          val temp = distances[j - 1]
          distances[j - 1] = prev
          prev = minOf(distances[j] + 1, distances[j - 1] + 1, temp + if (input[j - 1] == command[i - 1]) 0 else 1)
        }
        distances[input.length] = prev
      }
      
      if (distances[input.length] <= maxDistance) {
        result.add(command)
      }
    }
    
    return result
  }
}