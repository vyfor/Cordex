package me.blast.parser

import me.blast.command.argument.Argument
import me.blast.command.argument.builder.ArgumentType
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Utils.takeWhileWithIndex
import org.javacord.api.entity.Mentionable
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.entity.channel.ServerForumChannel
import org.javacord.api.entity.channel.ServerStageVoiceChannel
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.ServerThreadChannel
import org.javacord.api.entity.channel.ServerVoiceChannel
import org.javacord.api.entity.emoji.CustomEmoji
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.event.message.MessageCreateEvent
import kotlin.jvm.optionals.getOrNull
import kotlin.math.absoluteValue

object ArgumentsParser {
  @Throws(ArgumentException::class)
  fun parseTextCommand(input: List<String>, options: List<Argument<*>>, event: MessageCreateEvent, guildOnly: Boolean): Map<String, Any> {
    val args = input.listIterator()
    val validationList = options.toMutableList()
    val map = mutableMapOf<String, Any>()
    while (args.hasNext()) {
      val next = args.next()
      val arg = validationList.find {
        if (next.startsWith("--")) it.argumentName == next.substring(2)
        else if (next.startsWith('-')) it.argumentShortName == next.substring(1)
        else it.argumentType == ArgumentType.POSITIONAL
      }
      if (arg != null) {
        arg.guildOnly = guildOnly
        event.server.ifPresent { arg.argumentServer = it }
        arg.argumentChannel = event.channel
        arg.argumentUser = event.messageAuthor.asUser().get()
        when (arg.argumentType) {
          ArgumentType.FLAG -> {
            map[arg.argumentName!!] = true
            validationList.remove(arg)
          }
          
          ArgumentType.OPTION,
          ArgumentType.POSITIONAL,
          -> {
            if (arg.argumentRange.first != 1 || arg.argumentRange.last != 1) {
              val arguments = if (next.startsWith('-')) {
                args.takeWhileWithIndex { i, s ->
                  if (i == 0 && s.startsWith('-')) throw ArgumentException.Empty(arg)
                  (if (arg.argumentRange.last == 0) true else i < arg.argumentRange.last) && !s.startsWith('-')
                }
              } else {
                args.takeWhileIndexedWithElement(next) { i, s ->
                  if (i == 1 && s.startsWith('-')) throw ArgumentException.Empty(arg)
                  (if (arg.argumentRange.last == 0) true else i < arg.argumentRange.last) && !s.startsWith('-')
                }
              }
              if (arguments.isEmpty()) continue
              if (arg.argumentRange.first != 0 && arguments.size < arg.argumentRange.first) throw ArgumentException.Insufficient(arg, arguments.joinToString(" "))
              try {
                map[arg.argumentName!!] = arg.argumentValidator?.invoke(arguments.joinToString(" ")) ?: arg.argumentListValidator?.invoke(arguments) ?: arguments
                validationList.remove(arg)
              } catch (e: Exception) {
                throw ArgumentException.Invalid(arg, arguments.joinToString(" "), e.message)
              }
            } else {
              val nextArg: String
              if (arg.argumentType == ArgumentType.OPTION) {
                if (!args.hasNext()) throw ArgumentException.Empty(arg)
                else nextArg = args.next()
                if (nextArg.startsWith('-')) throw ArgumentException.Empty(arg)
              } else {
                nextArg = next
              }
              try {
                map[arg.argumentName!!] = arg.argumentValidator?.invoke(nextArg) ?: nextArg
                validationList.remove(arg)
              } catch (e: Exception) {
                throw ArgumentException.Invalid(arg, nextArg, e.message)
              }
            }
          }
        }
      }
    }
    val missingArgs = validationList.filter {
      if (it.argumentDefaultValue != null) {
        map[it.argumentName!!] = it.argumentDefaultValue!!
        false
      } else {
        !it.argumentIsOptional
      }
    }
    if (missingArgs.isNotEmpty()) throw ArgumentException.Missing(missingArgs)
    
    return map
  }
  
  @Throws(ArgumentException::class)
  fun parseSlashCommand(options: List<Argument<*>>, event: SlashCommandCreateEvent): Map<String, Any> {
    val args = event.slashCommandInteraction.arguments.listIterator()
    val map = mutableMapOf<String, Any>()
    options.forEach { arg ->
      event.slashCommandInteraction.server.ifPresent { arg.argumentServer = it }
      event.slashCommandInteraction.channel.ifPresent { arg.argumentChannel = it }
      arg.argumentUser = event.slashCommandInteraction.user
      map[arg.argumentName!!] = when (arg.argumentReturnValue) {
        String::class -> args.next().stringValue.get()
        Boolean::class -> args.next().booleanValue.get()
        Int::class, Long::class -> args.next().longValue.get()
        UInt::class, ULong::class -> args.next().longValue.get().absoluteValue
        Float::class, Double::class -> args.next().decimalValue.get()
        User::class -> args.next().userValue.get()
        ServerChannel::class -> args.next().channelValue.get()
        Role::class -> args.next().roleValue.get()
        Mentionable::class -> args.next().mentionableValue.get()
        ServerTextChannel::class -> args.next().channelValue.get().asServerTextChannel().get()
        ServerVoiceChannel::class -> args.next().channelValue.get().asServerVoiceChannel().get()
        ServerThreadChannel::class -> args.next().channelValue.get().asServerThreadChannel().get()
        ServerStageVoiceChannel::class -> args.next().channelValue.get().asServerStageVoiceChannel().get()
        ServerForumChannel::class -> args.next().channelValue.get().asServerForumChannel().get()
        ChannelCategory::class -> args.next().channelValue.get().asChannelCategory().get()
        else -> arg.argumentValidator!!.invoke(args.next().stringValue.get())!!
      }
    }
    
    return map
  }
  
  private fun <T> ListIterator<T>.takeWhileIndexedWithElement(element: T, predicate: (index: Int, T) -> Boolean): List<T> {
    val resultList = mutableListOf<T>()
    var currentIndex = 0
    
    resultList.add(element)
    
    while (hasNext()) {
      val item = next()
      if (predicate(currentIndex, item)) {
        resultList.add(item)
        currentIndex++
      } else {
        previous()
        break
      }
    }
    
    return resultList
  }
}