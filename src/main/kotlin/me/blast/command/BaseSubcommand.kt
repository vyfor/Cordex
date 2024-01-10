package me.blast.command

import me.blast.command.argument.Argument
import me.blast.command.text.TextSubcommand

interface BaseSubcommand {
  val name: String
  val description: String
  val options: ArrayList<Argument<*>>
  
  fun toTextSubcommand() = this as TextSubcommand
}