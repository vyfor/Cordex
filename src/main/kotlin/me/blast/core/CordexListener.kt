package me.blast.core

import kotlinx.coroutines.launch
import me.blast.command.argument.Arguments
import me.blast.command.Context
import me.blast.parser.ArgumentsParser
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Utils.generateArgumentError
import me.blast.utils.Utils.hasValue
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.awt.Color

class CordexListener(private val cordex: CordexBuilder) : MessageCreateListener {
  override fun onMessageCreate(event: MessageCreateEvent) {
    if (!event.messageAuthor.isRegularUser) return
    val prefix = cordex.config.prefix(event.server.orElse(null)?.id ?: -1)
    if (event.messageContent.startsWith(prefix)) {
      executeCommand(event, prefix)
    }
  }
  
  private fun executeCommand(event: MessageCreateEvent, prefix: String) {
    val args = event.messageContent.substring(prefix.length).split(Regex("\\s+"))
    cordex.handler.getCommands()[args[0].lowercase()]?.apply {
      if(guildOnly && !event.server.hasValue()) return
      try {
        val parsedArgs = ArgumentsParser.parse(args.drop(1), options, event, guildOnly)
        Cordex.scope.launch {
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
        }
      } catch (e: ArgumentException) {
        cordex.config.parsingErrorHandler?.invoke(event, this, e) ?: event.message.reply(
          EmbedBuilder().apply {
            setTitle(
              when (e) {
                is ArgumentException.Invalid -> "Invalid value provided for argument ${e.argument.argumentName}"
                is ArgumentException.Empty -> "No value provided for argument ${e.argument.argumentName}"
                is ArgumentException.Insufficient -> "Insufficient amount of values provided for argument ${e.argument.argumentName}"
                is ArgumentException.Missing -> "Missing required arguments: ${e.arguments.joinToString(", ") { it.argumentName }}"
              }
            )
            setDescription(generateArgumentError(e))
            setColor(Color.RED)
          }
        )
      } catch (e: Exception) {
        cordex.config.errorHandler?.invoke(event, this@apply, e)
        Cordex.logger.error("Error occurred while executing command $name", e)
      }
    }
  }
}