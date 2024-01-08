@file:Suppress("unused")

package me.blast.utils.extensions

inline fun <T> throwIf(condition: Boolean, block: () -> T): T {
  if (!condition) {
    return block()
  } else throw IllegalArgumentException()
}

inline fun <T> throwUnless(condition: Boolean, block: () -> T): T {
  if (condition) {
    return block()
  } else throw IllegalArgumentException()
}