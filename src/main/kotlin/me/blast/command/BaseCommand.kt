package me.blast.command

import me.blast.command.slash.SlashCommand
import me.blast.command.text.TextCommand
import kotlin.time.Duration

interface BaseCommand {
  val name: String
  val description: String
  
  fun toTextCommand() = this as TextCommand
  fun toSlashCommand() = this as SlashCommand
}