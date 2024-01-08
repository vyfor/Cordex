@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package me.blast.utils.command

import me.blast.command.text.TextCommand
import me.blast.command.argument.Argument
import me.blast.command.argument.builder.ArgumentType
import me.blast.command.slash.SlashCommand
import me.blast.parser.exception.ArgumentException
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandOptionBuilder
import org.javacord.api.interaction.SlashCommandOptionType
import java.awt.Color
import java.util.*
import kotlin.math.abs

object CommandUtils {
  fun TextCommand.generateHelpMessage(user: MessageAuthor) = EmbedBuilder().apply {
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
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName?.uppercase()}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
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
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName?.uppercase()}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Insufficient -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName?.uppercase()}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Invalid -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName?.uppercase()}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Missing -> {
        exception.arguments.partition { it.argumentType == ArgumentType.POSITIONAL }.run {
          buildString {
            if (first.isNotEmpty()) {
              append("Positional Arguments:\n")
              append(
                first.joinToString("\n") { option ->
                  "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
                }
              )
            }
            if (second.isNotEmpty()) {
              append("\n\nOptions:\n")
              append(
                second.joinToString("\n") { option ->
                  "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
                }
              )
            }
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
  
  fun generateSlashCommands(commands: Collection<SlashCommand>): Pair<Set<SlashCommandBuilder>, Map<Long, Set<SlashCommandBuilder>>> {
    fun createSlashCommand(command: SlashCommand): SlashCommandBuilder {
      return SlashCommandBuilder()
        .setName(command.name)
        .setDescription(command.description)
        .setEnabledInDms(!command.guildOnly)
        .setOptions(
          command.options.map { argument ->
            SlashCommandOptionBuilder()
              .setRequired(!argument.argumentIsOptional)
              .setName(argument.argumentName!!)
              .setDescription(argument.argumentDescription)
              .apply {
                if (argument.argumentChoices != null) argument.argumentChoices!!.forEach { (k, v) ->
                  addChoice(k, v)
                }
                when (argument.argumentReturnValue) {
                  Int::class -> {
                    setLongMinValue(Int.MIN_VALUE.toLong())
                    setMaxLength(Int.MAX_VALUE.toLong())
                  }
                  
                  UInt::class -> {
                    setLongMinValue(0)
                    setMaxLength(UInt.MAX_VALUE.toLong())
                  }
                  
                  ULong::class -> {
                    setLongMinValue(0)
                  }
                }
              }
              .setType(
                when (argument.argumentReturnValue) {
                  Boolean::class -> SlashCommandOptionType.BOOLEAN
                  Int::class, Long::class, UInt::class, ULong::class -> SlashCommandOptionType.LONG
                  Float::class, Double::class -> SlashCommandOptionType.DECIMAL
                  User::class -> SlashCommandOptionType.USER
                  ServerChannel::class -> SlashCommandOptionType.CHANNEL
                  Role::class -> SlashCommandOptionType.ROLE
                  else -> SlashCommandOptionType.STRING
                }
              )
              .build()
          }
        ).apply {
          if (!command.permissions.isNullOrEmpty()) setDefaultEnabledForPermissions(*command.permissions.toTypedArray())
        }
    }
    
    val (globalCommands, serverCommands) = commands.partition { it.guildId == null }
    return globalCommands.map(::createSlashCommand).toSet() to serverCommands
      .groupBy { it.guildId!!.toLong() }
      .mapValues { (_, cmd) ->
        cmd.map(::createSlashCommand).toSet()
      }
  }
}