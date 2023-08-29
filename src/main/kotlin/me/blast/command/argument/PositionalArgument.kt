@file:Suppress("")

package me.blast.command.argument

import me.blast.command.argument.builder.ArgumentType

class PositionalArgument<T>(options: ArrayList<Argument<*>>) : Argument<T>(options = options) {
  init {
    argumentType = ArgumentType.POSITIONAL
  }
  
  fun optional(): OptionalPositionalArgument<T?, T> {
    return OptionalPositionalArgument<T?, T>().apply {
      argumentIsOptional = true
    }
  }
  
  fun <R : Any> optional(default: R): OptionalPositionalArgument<R, R> {
    return OptionalPositionalArgument<R, R>().apply {
      argumentIsOptional = true
      argumentDefaultValue = default
    }
  }
  
  fun <R : Any> optional(validator: String.() -> R): OptionalPositionalArgument<R?, R> {
    return OptionalPositionalArgument<R?, R>().apply {
      argumentIsOptional = true
      argumentValidator = validator
    }
  }
  
  fun <R : Any> optional(default: R, validator: String.() -> R): OptionalPositionalArgument<R, R> {
    return OptionalPositionalArgument<R, R>().apply {
      argumentIsOptional = true
      argumentDefaultValue = default
      argumentValidator = validator
    }
  }
  
  fun multiple(range: IntRange = 0..0): MultiplePositionalArgument<List<T>> {
    return MultiplePositionalArgument<List<T>>().apply {
      argumentRange = range
    }
  }
  
  fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultiplePositionalArgument<R> {
    return MultiplePositionalArgument<R>().apply {
      argumentRange = range
      argumentListValidator = validator
    }
  }
  
  inner class OptionalPositionalArgument<T : Any?, C> : OptionalArgument<T>(this, options) {
    fun multiple(range: IntRange = 0..0): OptionalMultiValueArgument<List<C>> {
      return OptionalMultiValueArgument<List<C>>(this, options).apply {
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
  
  inner class MultiplePositionalArgument<T : Any?> : MultiValueArgument<T>(this, options) {
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