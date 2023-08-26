package me.blast.core

import kotlinx.coroutines.launch
import me.blast.command.Arguments
import me.blast.command.CommandImpl
import me.blast.command.Context
import me.blast.parser.ArgumentsParser
import me.blast.parser.exceptions.ArgumentException
import me.blast.utils.Utils.generateArgumentUsage
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.awt.Color

class CordexListener(private val cordex: CordexBuilder) : MessageCreateListener {
  override fun onMessageCreate(event: MessageCreateEvent) {
    if (!event.messageAuthor.isRegularUser) return
    val prefix = cordex.prefix(event.server.orElse(null)?.id ?: -1)
    if (event.messageContent.startsWith(prefix)) {
      executeCommand(event, prefix)
    }
  }
  
  private fun executeCommand(event: MessageCreateEvent, prefix: String) {
    val args = event.messageContent.substring(prefix.length).split(Regex("\\s+"))
    cordex.handler.getCommands()[args[0].lowercase()]?.apply {
      val parsedArgs: Map<CommandImpl.Delegate<*>, Any>
      try {
        parsedArgs = ArgumentsParser.parse(args.drop(1), options, event, guildOnly)
      } catch (e: ArgumentException) {
        cordex.handler.errorHandler?.invoke(e, this) ?: event.message.reply(
          EmbedBuilder().apply {
            setTitle(
              when (e) {
                is ArgumentException.Invalid -> "Invalid value provided for argument ${e.argument.name}"
                is ArgumentException.Empty -> "No value provided for argument ${e.argument.name}"
                is ArgumentException.Missing -> "Missing required arguments: ${e.arguments.joinToString(", ") { it.name }}"
              }
            )
            setColor(Color.RED)
            generateArgumentUsage(e)?.let { setDescription(it) }
          }
        )
        return
      }
      Cordex.scope.launch {
        try {
          execute(
            Context(
              event,
              event.server.get(),
              event.channel,
              event.messageAuthor.asUser().get(),
              event.message,
              prefix
            ),
            Arguments(parsedArgs)
          )
        } catch (e: Exception) {
          Cordex.logger.error("Error occurred while executing command $name", e)
        }
      }
    }
  }
}