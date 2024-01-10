package me.blast.command.slash

import me.blast.command.Arguments
import me.blast.command.BaseCommand
import me.blast.command.text.TextCommand
import me.blast.command.argument.builder.ArgumentBuilder
import org.javacord.api.entity.permission.PermissionType
import kotlin.time.Duration

abstract class SlashCommand(
  name: String,
  override val description: String = "No description provided",
  val type: String? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val subcommands: List<TextCommand>? = null,
  val userCooldown: Duration = Duration.ZERO,
  val channelCooldown: Duration = Duration.ZERO,
  val serverCooldown: Duration = Duration.ZERO,
  val isNsfw: Boolean = false,
  val guildId: Long? = null,
) : ArgumentBuilder(guildId != null), BaseCommand {
  override val name = name.lowercase()
  var applyCooldown = true
  
  init {
    if (
      userCooldown.isNegative() ||
      channelCooldown.isNegative() ||
      serverCooldown.isNegative()
    ) throw IllegalArgumentException("Cooldown cannot be negative.")
  }
  
  /**
   * When called, the cooldown for the command will not be applied.
   */
  fun revokeCooldown() {
    applyCooldown = false
  }
  
  abstract suspend fun Arguments.execute(ctx: SlashContext)
}