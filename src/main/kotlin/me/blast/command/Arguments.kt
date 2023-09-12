@file:Suppress("UNCHECKED_CAST")

package me.blast.command

import me.blast.command.argument.Argument

class Arguments(@PublishedApi internal val args: Map<String, Any?>) {
  val <T> Argument<T>.value: T
    get() {
      return args[argumentName] as T
    }
  
  operator fun <T> Argument<T>.invoke(): T {
    return args[argumentName] as T
  }
  
  inline operator fun <T, R> Argument<T>.invoke(action: (T) -> R): R {
    return action(args[argumentName] as T)
  }
}