@file:Suppress("unused")

package me.blast.command.dsl

import me.blast.command.Arguments
import me.blast.command.Command
import me.blast.command.Context
import org.javacord.api.entity.permission.PermissionType

fun command(
  name: String,
  block: CommandBuilder.() -> Unit,
): Command {
  val builder = CommandBuilder(name)
  block.invoke(CommandBuilder(name))
  return builder.build()
}

class CommandBuilder(val name: String) {
  var description: String = "No description provided."
  var aliases: List<String>? = null
  var cooldown: Long = 0
  var type: String? = null
  var permissions: List<PermissionType>? = null
  var selfPermissions: List<PermissionType>? = null
  var subcommands: List<Command>? = null
  var runAsDefault: Boolean = false
  var guildOnly: Boolean = false
  private lateinit var execute: suspend Arguments.(Context) -> Unit
  
  fun execute(block: suspend Arguments.(Context) -> Unit) {
    execute = block
  }
  
  fun build(): Command {
    return object : Command(name, description, aliases, cooldown, type, permissions, selfPermissions, subcommands, runAsDefault, guildOnly) {
      override suspend fun Arguments.execute(ctx: Context) {
        this@CommandBuilder.execute(this, ctx)
      }
    }
  }
}