package me.blast.core

import kotlinx.coroutines.launch
import me.blast.command.Arguments
import me.blast.command.Context
import me.blast.parser.ArgumentsParser
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Embeds
import me.blast.utils.Utils.hasValue
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

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
      if (guildOnly && !event.server.hasValue()) return@apply
      
      try {
        // Check for the user's permissions in the server.
        if (
          permissions != null &&
          !(
            event.server.get().isAdmin(event.messageAuthor.asUser().get()) ||
            event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).containsAll(permissions)
           )
        ) event.message.reply(Embeds.missingPermissions(permissions))
        // Check for the bot's permissions in the server.
        if (
          selfPermissions != null &&
          !(
            event.server.get().isAdmin(event.api.yourself) ||
            event.server.get().getAllowedPermissions(event.api.yourself).containsAll(selfPermissions)
           )
        ) event.message.reply(Embeds.missingSelfPermissions(selfPermissions))
        val parsedArgs = ArgumentsParser.parse(args.drop(1), options, event, guildOnly)
        Cordex.scope.launch {
          Arguments(parsedArgs).execute(
            Context(
              event,
              event.server.get(),
              event.channel,
              event.messageAuthor.asUser().get(),
              event.message,
              prefix
            )
          )
        }
      } catch (e: ArgumentException) {
        cordex.config.parsingErrorHandler?.invoke(event, this, e)
        ?: event.message.reply(Embeds.invalidArguments(e))
      } catch (e: Exception) {
        cordex.config.errorHandler?.invoke(event, this@apply, e)
        Cordex.logger.error("Error occurred while executing command $name", e)
      }
    }
  }
}