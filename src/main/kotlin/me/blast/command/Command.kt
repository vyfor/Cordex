package me.blast.command

import me.blast.command.argument.builder.ArgumentBuilder
import org.javacord.api.entity.permission.PermissionType
import kotlin.time.Duration

abstract class Command(
  name: String,
  val description: String = "No description provided.",
  aliases: List<String>? = null,
  val type: String? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val subcommands: List<Command>? = null,
  val userCooldown: Duration = Duration.ZERO,
  val channelCooldown: Duration = Duration.ZERO,
  val serverCooldown: Duration = Duration.ZERO,
  val runAsDefault: Boolean = false,
  override val guildOnly: Boolean = false,
) : ArgumentBuilder(guildOnly) {
  val name = name.lowercase()
  val aliases = aliases?.map { it.lowercase() }
  
  init {
    if (
      userCooldown.isNegative() ||
      channelCooldown.isNegative() ||
      serverCooldown.isNegative()
    ) throw IllegalArgumentException("Cooldown cannot be negative.")
  }
  
  abstract suspend fun Arguments.execute(ctx: Context)
}