package me.blast.utils.command

import me.blast.parser.exception.ArgumentException
import me.blast.utils.command.CommandUtils.generateArgumentError
import me.blast.utils.cooldown.CooldownType
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.PermissionType
import java.awt.Color

object Embeds {
  fun invalidArguments(e: ArgumentException) = EmbedBuilder().apply {
    setTitle(
      when (e) {
        is ArgumentException.Invalid -> "Invalid value provided for argument '${e.argument.argumentName}'"
        is ArgumentException.Empty -> "No value provided for argument '${e.argument.argumentName}'"
        is ArgumentException.Insufficient -> "Insufficient amount of values provided for argument '${e.argument.argumentName}'"
        is ArgumentException.Missing -> "Missing required arguments: ${e.arguments.joinToString(prefix = "'", separator = "', '", postfix = "'") { it.argumentName!! }}"
      }
    )
    setDescription("${if (e.message != null) "> ${e.message}\n" else ""}```ansi\n${generateArgumentError(e)}\n```")
    setColor(Color.RED)
  }
  
  fun missingPermissions(permissions: List<PermissionType>) = EmbedBuilder().apply {
    setTitle("You're missing one or more of the following permissions!")
    setDescription(permissions.joinToString(", ") { "`${it.name}`" })
    setColor(Color.RED)
  }
  
  fun missingSelfPermissions(permissions: List<PermissionType>) = EmbedBuilder().apply {
    setTitle("I'm missing one or more of the following permissions!")
    setDescription(permissions.joinToString(", ") { "`${it.name}`" })
    setColor(Color.RED)
  }
  
  fun commandNotFound(command: String, suggestions: Set<String>, prefix: String) = EmbedBuilder().apply {
    setTitle("Command with name `$command` could not be found!")
    setDescription("Did you mean:\n${suggestions.joinToString("\n") { "`$prefix$it`" }}")
    setColor(Color.RED)
  }
  
  fun userHitCooldown(timeLeft: Long, cooldownType: CooldownType) = EmbedBuilder().apply {
    setTitle(
      when (cooldownType) {
        CooldownType.USER -> "You are"
        CooldownType.CHANNEL -> "This channel is"
        CooldownType.SERVER -> "This server is"
      } + " currently on a cooldown for this command"
    )
    setDescription("The command can be used again in ${String.format("%.1f", timeLeft / 1000.0)} seconds.")
    setColor(Color.RED)
  }
}