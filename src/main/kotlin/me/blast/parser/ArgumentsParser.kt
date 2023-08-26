package me.blast.parser

import me.blast.command.CommandImpl
import me.blast.parser.exceptions.ArgumentException
import me.blast.utils.Utils.takeWhileWithIndex
import org.javacord.api.event.message.MessageCreateEvent

object ArgumentsParser {
  @Throws(ArgumentException::class, IllegalArgumentException::class)
  fun parse(input: List<String>, options: List<CommandImpl.Delegate<*>>, event: MessageCreateEvent, guildOnly: Boolean): Map<CommandImpl.Delegate<*>, Any> {
    val args = input.listIterator()
    val validationList = options.toMutableList()
    val map = mutableMapOf<CommandImpl.Delegate<*>, Any>()
    while (args.hasNext()) {
      val arg = args.next()
      if (arg.startsWith('-')) {
        val temp = arg.substring(1)
        if (temp.startsWith('-')) {
          validationList.remove(validationList.find { it.name == temp.substring(1) }?.apply {
            map[this] = when (this) {
              is CommandImpl.FlagDelegate -> true
              is CommandImpl.OptionDelegate,
              is CommandImpl.OptionDelegate<*>.OptionalOptionDelegate<*, *>,
              -> {
                val nextArg = args.next()
                if (nextArg.startsWith('-')) throw ArgumentException.Empty(this)
                try {
                  validator?.invoke(nextArg) ?: nextArg
                } catch (_: Exception) {
                  throw ArgumentException.Invalid(this)
                }
              }
              
              is CommandImpl.OptionDelegate<*>.MultipleOptionDelegate<*, *> -> {
                val arguments = args.takeWhileWithIndex { i, s ->
                  if (i == 0 && s.startsWith('-')) throw ArgumentException.Empty(this)
                  (if (multipleValues == 0) true else i < multipleValues) && !s.startsWith('-')
                }
                try {
                  listValidator?.invoke(arguments) ?: arguments
                } catch (_: Exception) {
                  throw ArgumentException.Invalid(this)
                }
              }
              
              else -> throw IllegalArgumentException()
            }
            if (guildOnly) this.event = event
          })
        } else {
          validationList.remove(validationList.find { it.short == temp }?.apply {
            map[this] = when (this) {
              is CommandImpl.FlagDelegate -> true
              is CommandImpl.OptionDelegate,
              is CommandImpl.OptionDelegate<*>.OptionalOptionDelegate<*, *>,
              -> {
                val nextArg = args.next()
                if (nextArg.startsWith('-')) throw ArgumentException.Empty(this)
                try {
                  validator?.invoke(nextArg) ?: nextArg
                } catch (_: Exception) {
                  throw ArgumentException.Invalid(this)
                }
              }
              
              is CommandImpl.OptionDelegate<*>.MultipleOptionDelegate<*, *> -> {
                val arguments = args.takeWhileWithIndex { i, s ->
                  if (i == 0 && s.startsWith('-')) throw ArgumentException.Empty(this)
                  (if (multipleValues == 0) true else i < multipleValues) && !s.startsWith('-')
                }
                try {
                  listValidator?.invoke(arguments) ?: arguments
                } catch (_: Exception) {
                  throw ArgumentException.Invalid(this)
                }
              }
              
              else -> throw IllegalArgumentException()
            }
            if (guildOnly) this.event = event
          })
        }
      } else {
        validationList.remove(validationList.find { it is CommandImpl.PositionalDelegate || it is CommandImpl.PositionalDelegate<*>.OptionalPositionalDelegate<*, *> || it is CommandImpl.PositionalDelegate<*>.MultiplePositionalDelegate<*, *> }?.apply {
          map[this] = when (this) {
            is CommandImpl.PositionalDelegate<*>,
            is CommandImpl.PositionalDelegate<*>.OptionalPositionalDelegate<*, *>,
            -> {
              val nextArg = args.next()
              if (nextArg.startsWith('-')) throw ArgumentException.Empty(this)
              try {
                validator?.invoke(nextArg) ?: nextArg
              } catch (_: Exception) {
                throw ArgumentException.Invalid(this)
              }
            }
            
            is CommandImpl.PositionalDelegate<*>.MultiplePositionalDelegate<*, *> -> {
              val arguments = args.takeWhileWithIndex { i, s ->
                if (i == 0 && s.startsWith('-')) throw ArgumentException.Empty(this)
                (if (multipleValues == 0) true else i < multipleValues) && !s.startsWith('-')
              }
              try {
                listValidator?.invoke(arguments) ?: arguments
              } catch (_: Exception) {
                throw ArgumentException.Invalid(this)
              }
            }
            
            else -> throw IllegalArgumentException()
          }
          if (guildOnly) this.event = event
        })
      }
    }
    val missingArgs = validationList.filter { if (it is CommandImpl.FlagDelegate) false else !it.isOptional }
    if (missingArgs.isNotEmpty()) {
      throw ArgumentException.Missing(missingArgs)
    }
    
    return map
  }
}