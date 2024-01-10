@file:Suppress("unused")

package me.blast.command.dsl

import me.blast.command.Arguments
import me.blast.command.text.TextCommand
import me.blast.command.text.TextContext
import me.blast.command.text.TextSubcommand
import org.javacord.api.entity.permission.PermissionType
import kotlin.time.Duration

@DslMarker
annotation class CommandDsl

fun command(name: String, block: CommandBuilder.() -> Unit): TextCommand {
  val builder = CommandBuilder(name)
  builder.block()
  return builder.build()
}

@CommandDsl
class CommandBuilder(val name: String) {
  val subcommands by lazy { mutableListOf<TextSubcommand>() }
  var description: String = "No description provided."
  var aliases: List<String>? = null
  var type: String? = null
  var permissions: List<PermissionType>? = null
  var selfPermissions: List<PermissionType>? = null
  var runAsDefault: Boolean = false
  var userCooldown: Duration = Duration.ZERO
  var channelCooldown: Duration = Duration.ZERO
  var serverCooldown: Duration = Duration.ZERO
  var guildOnly: Boolean = false
  
  lateinit var executeBlock: suspend Arguments.(ctx: TextContext) -> Unit
  
  fun execute(block: suspend Arguments.(ctx: TextContext) -> Unit) {
    executeBlock = block
  }
  
  fun build(): TextCommand {
    return object : TextCommand(name, description, aliases, type, permissions, selfPermissions, userCooldown, channelCooldown, serverCooldown, runAsDefault, guildOnly) {
      override suspend fun Arguments.execute(ctx: TextContext) {
        executeBlock(this, ctx)
      }
    }
  }
}