@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")

package me.blast.utils

import java.time.Instant

class Snowflake(val id: Long) {
  val creationDate: Instant by lazy {
    Instant.ofEpochMilli((id shr 22) + 1420070400000)
  }
  val workerId: Long by lazy {
    (id and 0x3E0000) shr 17
  }
  val processId: Long by lazy {
    (id and 0x1F000) shr 12
  }
  val sequence: Long by lazy {
    id and 0xFFF
  }
}