package me.blast.command

import org.javacord.api.entity.permission.PermissionType

abstract class Command(
  val name: String,
  val description: String = "No description provided.",
  val aliases: List<String>? = null,
  val cooldown: Long = 0,
  val type: String? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val subcommands: List<Command>? = null,
  val runAsDefault: Boolean = false
) {
  val remainingCooldown: Int
    get() = 0
  
  abstract fun execute(ctx: Context)
}