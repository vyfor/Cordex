@file:Suppress("unused")

package me.blast.command.dsl

import me.blast.command.Arguments
import me.blast.command.argument.Argument
import me.blast.command.argument.builder.SubArgumentBuilder
import me.blast.command.text.TextContext
import me.blast.command.text.TextSubcommand
import org.javacord.api.entity.permission.PermissionType

fun subcommand(name: String, block: SubcommandBuilder.() -> Unit, ): TextSubcommand {
  val builder = SubcommandBuilder(name)
  builder.block()
  return builder.build()
}

class SubcommandBuilder(val name: String, override var guildOnly: Boolean = false): SubArgumentBuilder(guildOnly) {
  var description: String = "No description provided."
  var aliases: List<String>? = null
  var permissions: List<PermissionType>? = null
  var selfPermissions: List<PermissionType>? = null
  var options: ArrayList<Argument<*>> = arrayListOf()
    private set
  private lateinit var execute: suspend Arguments.(String, TextContext) -> Unit
  
  fun execute(block: suspend Arguments.(String, TextContext) -> Unit) {
    execute = block
  }
  
  fun build(): TextSubcommand {
    return object : TextSubcommand(name, description, aliases, permissions, selfPermissions, guildOnly) {
      init {
        options = this@SubcommandBuilder.options
      }
      
      override suspend fun Arguments.execute(parent: String, ctx: TextContext) {
        this@SubcommandBuilder.execute(this, parent, ctx)
      }
    }
  }
}

interface Subcommand