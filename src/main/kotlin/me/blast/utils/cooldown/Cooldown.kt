package me.blast.utils.cooldown

import kotlinx.coroutines.Job

data class Cooldown(
  val job: Job,
  val endTime: Long,
)

enum class CooldownType {
  USER,
  CHANNEL,
  SERVER
}