@file:Suppress("UNCHECKED_CAST")

package me.blast.command

import me.blast.command.argument.Argument
import me.blast.utils.Utils.lazyEmptyList

class Arguments(@PublishedApi internal val args: Map<String, Any>) {
  val <T> Argument<T>.value: T
    get() {
      return args[argumentName] as T
    }
  val <T> Argument<List<T>>.value: List<T>
    get() {
      return args[argumentName] as? List<T> ?: lazyEmptyList as List<T>
    }
  
  operator fun <T> Argument<T>.invoke(): T {
    return args[argumentName] as T
  }
  
  operator fun <T> Argument<List<T>>.invoke(): List<T> {
    return args[argumentName] as? List<T> ?: lazyEmptyList as List<T>
  }
  
  inline operator fun <T, R> Argument<T>.invoke(action: (T) -> R): R {
    return action(args[argumentName] as T)
  }
  
  inline operator fun <T, R> Argument<List<T>>.invoke(action: (List<T>) -> R): R {
    return action(args[argumentName] as? List<T> ?: lazyEmptyList as List<T>)
  }
}