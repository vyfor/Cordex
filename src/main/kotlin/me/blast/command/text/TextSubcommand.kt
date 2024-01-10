package me.blast.command.text

import me.blast.command.Arguments
import me.blast.command.BaseSubcommand
import me.blast.command.argument.Argument
import me.blast.command.argument.builder.SubArgumentBuilder
import org.javacord.api.entity.permission.PermissionType

abstract class TextSubcommand(
  name: String,
  override val description: String = "No description provided",
  aliases: List<String>? = null,
  val permissions: List<PermissionType>? = null,
  val selfPermissions: List<PermissionType>? = null,
  val isNsfw: Boolean = false,
  override val guildOnly: Boolean = false,
  override var options: ArrayList<Argument<*>> = arrayListOf()
) : SubArgumentBuilder(guildOnly), BaseSubcommand {
  override val name = name.lowercase()
  val aliases = aliases?.map { it.lowercase() }
  
  abstract suspend fun Arguments.execute(parent: String, ctx: TextContext)
}