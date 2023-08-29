@file:Suppress("unused")

package me.blast.command.argument

import me.blast.command.argument.builder.ArgumentType

class OptionArgument<T>(options: ArrayList<Argument<*>>) : Argument<T>(options = options) {
  init {
    argumentType = ArgumentType.OPTION
  }
  
  fun optional(): OptionalOptionArgument<T?, T> {
    return OptionalOptionArgument<T?, T>().apply {
      argumentIsOptional = true
    }
  }
  
  fun <R : Any> optional(default: R): OptionalOptionArgument<R, R> {
    return OptionalOptionArgument<R, R>().apply {
      argumentIsOptional = true
      argumentDefaultValue = default
    }
  }
  
  fun <R : Any> optional(validator: String.() -> R): OptionalOptionArgument<R?, R> {
    return OptionalOptionArgument<R?, R>().apply {
      argumentIsOptional = true
      argumentValidator = validator
    }
  }
  
  fun <R : Any> optional(default: R, validator: String.() -> R): OptionalOptionArgument<R, R> {
    return OptionalOptionArgument<R, R>().apply {
      argumentIsOptional = true
      argumentDefaultValue = default
      argumentValidator = validator
    }
  }
  
  fun multiple(range: IntRange = 0..0): MultipleOptionArgument<List<T>> {
    return MultipleOptionArgument<List<T>>().apply {
      argumentRange = range
    }
  }
  
  fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultipleOptionArgument<T> {
    return MultipleOptionArgument<T>().apply {
      argumentRange = range
      argumentListValidator = validator
    }
  }
  
  inner class OptionalOptionArgument<T : Any?, S> : OptionalArgument<T>(this, options) {
    fun multiple(range: IntRange = 0..0): OptionalMultiValueArgument<List<S>> {
      return OptionalMultiValueArgument<List<S>>(this, options).apply {
        argumentRange = range
      }
    }
    
    fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): OptionalMultiValueArgument<R> {
      return OptionalMultiValueArgument<R>(this, options).apply {
        argumentRange = range
        argumentListValidator = validator
      }
    }
  }
  
  inner class MultipleOptionArgument<T : Any?> : MultiValueArgument<T>(this, options) {
    fun optional(): OptionalMultiValueArgument<T> {
      return OptionalMultiValueArgument<T>(this, options).apply {
        argumentIsOptional = true
      }
    }
    
    fun <R : Any> optional(default: R): OptionalMultiValueArgument<R> {
      return OptionalMultiValueArgument<R>(this, options).apply {
        argumentIsOptional = true
        argumentDefaultValue = default
      }
    }
    
    fun <R : Any> optional(validator: String.() -> R): OptionalMultiValueArgument<R> {
      return OptionalMultiValueArgument<R>(this, options).apply {
        argumentIsOptional = true
        argumentValidator = validator
      }
    }
    
    fun <R : Any> optional(default: R, validator: String.() -> R): OptionalMultiValueArgument<R> {
      return OptionalMultiValueArgument<R>(this, options).apply {
        argumentIsOptional = true
        argumentDefaultValue = default
        argumentValidator = validator
      }
    }
  }
}