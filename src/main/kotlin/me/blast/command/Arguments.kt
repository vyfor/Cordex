@file:Suppress("UNCHECKED_CAST")

package me.blast.command

import me.blast.command.argument.Argument
import me.blast.utils.Utils.lazyEmptyList

class Arguments(@PublishedApi internal val args: Map<String, Any>) {
  operator fun <T> get(key: Argument<List<T>>): List<T> {
    return args[key.argumentName] as? List<T> ?: lazyEmptyList as List<T>
  }
  
  operator fun <T> get(key: Argument<T>): T {
    return args[key.argumentName] as T
  }
}