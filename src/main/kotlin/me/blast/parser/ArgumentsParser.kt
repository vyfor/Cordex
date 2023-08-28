package me.blast.parser

import me.blast.command.CommandImpl
import me.blast.command.argument.ArgumentType
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Utils.takeWhileWithIndex
import org.javacord.api.event.message.MessageCreateEvent

object ArgumentsParser {
  @Throws(ArgumentException::class, IllegalArgumentException::class)
  fun parse(input: List<String>, options: List<CommandImpl.Delegate<*>>, event: MessageCreateEvent, guildOnly: Boolean): Map<String, Any> {
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
        if (guildOnly) arg.argumentEvent = event
        map[arg.argumentName] = when (arg.argumentType) {
          ArgumentType.FLAG -> true
          
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
              if(arg.argumentRange.first != 0 && arguments.size < arg.argumentRange.first) throw ArgumentException.Insufficient(arg, arguments.joinToString(" "))
              try {
                arg.argumentValidator?.invoke(arguments.joinToString(" ")) ?: arg.argumentListValidator?.invoke(arguments) ?: arg.argumentDefaultValue ?: throw ArgumentException.Invalid(arg, arguments.joinToString(" "))
              } catch (e: Exception) {
                e.printStackTrace()
                throw ArgumentException.Invalid(arg, arguments.joinToString(" "))
              }
            } else {
              val nextArg = if(arg.argumentType == ArgumentType.OPTION) {
                if(!args.hasNext()) throw ArgumentException.Empty(arg)
                args.next()
              } else {
                next
              }
              if (nextArg.startsWith('-')) throw ArgumentException.Empty(arg)
              try {
                arg.argumentValidator?.invoke(nextArg) ?: arg.argumentDefaultValue ?: throw ArgumentException.Invalid(arg, nextArg)
              } catch (e: Exception) {
                e.printStackTrace()
                throw ArgumentException.Invalid(arg, nextArg)
              }
            }
          }
        }
        validationList.remove(arg)
      }
    }
    val missingArgs = validationList.filter { if (it.argumentType == ArgumentType.FLAG) false else !it.argumentIsOptional }
    if (missingArgs.isNotEmpty()) {
      throw ArgumentException.Missing(missingArgs)
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