package me.blast.command.text

import me.blast.command.Arguments
import me.blast.command.BaseCommand
import me.blast.command.argument.builder.ArgumentBuilder
import org.javacord.api.entity.permission.PermissionType
import kotlin.time.Duration

abstract class TextCommand(
  name: String,
  override val description: String = "No description provided",
  aliases: List<String>? = null,
  val type: String? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val userCooldown: Duration = Duration.ZERO,
  val channelCooldown: Duration = Duration.ZERO,
  val serverCooldown: Duration = Duration.ZERO,
  val isNsfw: Boolean = false,
  override val guildOnly: Boolean = false,
) : ArgumentBuilder(guildOnly), BaseCommand {
  open val subcommands by lazy { mutableMapOf<String, TextSubcommand>() }
  override val name = name.lowercase()
  val aliases = aliases?.map { it.lowercase() }
  
  init {
    if (
      userCooldown.isNegative() ||
      channelCooldown.isNegative() ||
      serverCooldown.isNegative()
    ) throw IllegalArgumentException("Cooldown cannot be negative.")
  }
  
  abstract suspend fun Arguments.execute(ctx: TextContext)
  
//  /**
//   * When called, the cooldown for the command will not be applied.
//   */
//  fun revokeCooldown() {} // todo
  
  operator fun TextSubcommand.unaryPlus() {
    if (subcommands.containsKey(name)) throw RuntimeException("Subcommand with name '${name}' already exists for command '${this@TextCommand.name}'!")
    subcommands[name] = this
    aliases?.forEach {
      if (subcommands.containsKey(it)) throw RuntimeException("Subcommand alias with name '${it}' already exists for command '${this@TextCommand.name}'!")
      subcommands[it] = this
    }
  }
}