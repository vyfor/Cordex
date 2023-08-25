package me.blast.command

import org.javacord.api.entity.permission.PermissionType

abstract class Command(
  name: String,
  val description: String = "No description provided.",
  aliases: List<String>? = null,
  val cooldown: Long = 0,
  val type: String? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val subcommands: List<Command>? = null,
  val runAsDefault: Boolean = false,
  override val guildOnly: Boolean = false,
): CommandImpl(guildOnly) {
  val name = name.lowercase()
  val aliases = aliases?.map { it.lowercase() }
  
  abstract suspend fun execute(ctx: Context, args: Arguments)
}