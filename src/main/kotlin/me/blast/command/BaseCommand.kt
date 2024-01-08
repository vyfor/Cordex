package me.blast.command

import me.blast.command.slash.SlashCommand
import me.blast.command.text.TextCommand

interface BaseCommand {
  val name: String
  val description: String
  
  fun toTextCommand() = this as TextCommand
  fun toSlashCommand() = this as SlashCommand
}