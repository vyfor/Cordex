@file:Suppress("unused")

package me.blast.command.dsl

import me.blast.command.Arguments
import me.blast.command.text.TextCommand
import me.blast.command.text.TextContext
import org.javacord.api.entity.permission.PermissionType
import kotlin.time.Duration

fun command(
  name: String,
  block: CommandBuilder.() -> Unit,
): TextCommand {
  val builder = CommandBuilder(name)
  block.invoke(CommandBuilder(name))
  return builder.build()
}

class CommandBuilder(val name: String) {
  var description: String = "No description provided."
  var aliases: List<String>? = null
  var type: String? = null
  var permissions: List<PermissionType>? = null
  var selfPermissions: List<PermissionType>? = null
  var subcommands: List<TextCommand>? = null
  var runAsDefault: Boolean = false
  var userCooldown: Duration = Duration.ZERO
  var channelCooldown: Duration = Duration.ZERO
  var serverCooldown: Duration = Duration.ZERO
  var guildOnly: Boolean = false
  private lateinit var execute: suspend Arguments.(TextContext) -> Unit
  
  fun execute(block: suspend Arguments.(TextContext) -> Unit) {
    execute = block
  }
  
  fun build(): TextCommand {
    return object : TextCommand(name, description, aliases, type, permissions, selfPermissions, subcommands, userCooldown, channelCooldown, serverCooldown, runAsDefault, guildOnly) {
      override suspend fun Arguments.execute(ctx: TextContext) {
        this@CommandBuilder.execute(this, ctx)
      }
    }
  }
}