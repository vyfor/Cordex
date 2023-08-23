package me.blast.parser

import me.blast.command.CommandImpl
import me.blast.parser.exceptions.ArgumentException
import me.blast.utils.Utils.takeWhileWithIndex
import java.util.*

object ArgumentsParser {
  private fun String.isArg() = startsWith("-")
  
  fun parse(input: List<String>, options: LinkedList<CommandImpl.Delegate<*>>) {
    val args = input.listIterator()
    val validationList = options.toMutableList()
    while (args.hasNext()) {
      val arg = args.next()
      if (arg.isArg()) {
        val temp = arg.substring(1)
        if (temp.isArg()) {
          validationList.remove(validationList.find { it.name == temp.substring(1) }?.apply {
            when (this) {
              is CommandImpl.FlagDelegate -> {
                value = true
              }
              is CommandImpl.OptionDelegate,
              is CommandImpl.OptionDelegate<*>.OptionalOptionDelegate<*, *> -> {
                val nextArg = args.next()
                if (nextArg.isArg()) throw ArgumentException.Empty(this)
                validate(nextArg)
              }
              is CommandImpl.OptionDelegate<*>.MultipleOptionDelegate<*, *> -> {
                val arguments = args.takeWhileWithIndex { i, s ->
                  if (i == 0 && s.isArg()) throw ArgumentException.Empty(this)
                  (if(multipleValues == 0) true else i < multipleValues) && !s.isArg()
                }
                validate(arguments)
              }
              
              else -> throw IllegalArgumentException()
            }
          })
        } else {
          validationList.remove(validationList.find { it.short == temp }?.apply {
            when (this) {
              is CommandImpl.FlagDelegate -> {
                value = true
              }
              is CommandImpl.OptionDelegate,
              is CommandImpl.OptionDelegate<*>.OptionalOptionDelegate<*, *> -> {
                val nextArg = args.next()
                if (nextArg.isArg()) throw ArgumentException.Empty(this)
                validate(nextArg)
              }
              is CommandImpl.OptionDelegate<*>.MultipleOptionDelegate<*, *> -> {
                val arguments = args.takeWhileWithIndex { i, s ->
                  if (i == 0 && s.isArg()) throw ArgumentException.Empty(this)
                  (if(multipleValues == 0) true else i < multipleValues) && !s.isArg()
                }
                validate(arguments)
              }
              
              else -> throw IllegalArgumentException()
            }
          })
        }
      } else {
        validationList.remove(validationList.find { it is CommandImpl.PositionalDelegate }?.apply {
          val arguments = args.takeWhileWithIndex { i, s ->
            if (i == 0 && s.isArg()) throw ArgumentException.Empty(this)
            (if(multipleValues == 0) true else i < multipleValues) && !s.isArg()
          }
          validate(arguments.joinToString(" "))
        })
      }
    }
    val missingArgs = validationList.filter { if(it is CommandImpl.FlagDelegate) false else !it.isOptional }
    if(missingArgs.isNotEmpty()) {
      throw ArgumentException.Missing(missingArgs)
    }
  }
}