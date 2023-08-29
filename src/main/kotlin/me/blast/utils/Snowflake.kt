@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")

package me.blast.utils

import java.time.Instant

class Snowflake(val id: ULong) {
  val creationDate = Instant.ofEpochSecond(((id.toLong() shr 22) + 1420070400000) / 1000)
  val workerId = (id and 0x3E0000u) shr 17
  val processId = (id and 0x1F000u) shr 12
  val sequence = id and 0xFFFu
}