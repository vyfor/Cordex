package me.blast.parser

import me.blast.command.argument.Argument
import me.blast.command.argument.builder.ArgumentType
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Utils.takeWhileWithIndex
import org.javacord.api.event.message.MessageCreateEvent

object ArgumentsParser {
  @Throws(ArgumentException::class, IllegalArgumentException::class)
  fun parse(input: List<String>, options: List<Argument<*>>, event: MessageCreateEvent, guildOnly: Boolean): Map<String, Any> {
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
      if(arg != null) {
        arg.guildOnly = guildOnly
        if (guildOnly) {
          arg.argumentEvent = event
        }
       when (arg.argumentType) {
          ArgumentType.FLAG -> {
            map[arg.argumentName!!] = true
            validationList.remove(arg)
          }
          
          ArgumentType.OPTION,
          ArgumentType.POSITIONAL
          -> {
            if(arg.argumentRange.first != 1 || arg.argumentRange.last != 1) {
              val arguments = if(arg.argumentType == ArgumentType.OPTION) {
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
              if(arguments.isEmpty()) continue
              if(arg.argumentRange.first != 0 && arguments.size < arg.argumentRange.first) throw ArgumentException.Insufficient(arg, arguments.joinToString(" "))
              try {
                map[arg.argumentName!!] = arg.argumentValidator?.invoke(arguments.joinToString(" ")) ?: arg.argumentListValidator?.invoke(arguments) ?: arguments
                validationList.remove(arg)
              } catch (e: Exception) {
                throw ArgumentException.Invalid(arg, arguments.joinToString(" "))
              }
            } else {
              val nextArg: String
              if(arg.argumentType == ArgumentType.OPTION) {
                if(!args.hasNext()) throw ArgumentException.Empty(arg)
                else nextArg = args.next()
                if(nextArg.startsWith('-')) throw ArgumentException.Empty(arg)
              } else {
                nextArg = next
              }
              try {
                map[arg.argumentName!!] = arg.argumentValidator?.invoke(nextArg) ?: nextArg
                validationList.remove(arg)
              } catch (e: Exception) {
                throw ArgumentException.Invalid(arg, nextArg)
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
    if(missingArgs.isNotEmpty()) throw ArgumentException.Missing(missingArgs)
    
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