@file:Suppress("unused")

package me.blast.utils.command.suggestions

enum class DistanceAccuracy(val maxDistance: Int) {
  RIGID(1),
  STRICT(2),
  MODERATE(3),
  LENIENT(4),
  LOOSE(5)
}