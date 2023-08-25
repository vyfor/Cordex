package me.blast.core

import kotlinx.coroutines.launch
import me.blast.command.Arguments
import me.blast.command.Context
import me.blast.parser.ArgumentsParser
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

class CordexListener(private val cordex: CordexBuilder): MessageCreateListener {
  override fun onMessageCreate(event: MessageCreateEvent) {
    if(!event.messageAuthor.isRegularUser) return
    
    val prefix = cordex.prefix(event.server.orElse(null)?.id ?: -1)
    if(event.messageContent.startsWith(prefix)) {
      executeCommand(event, prefix)
    }
  }
  
  fun executeCommand(event: MessageCreateEvent, prefix: String) {
    val args = event.messageContent.substring(prefix.length).split(Regex("\\s+"))
    
    cordex.handler.getCommands()[args[0].lowercase()]?.apply {
      val parsedMap = ArgumentsParser.parse(args.drop(1), options, event, guildOnly)
      
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
          Arguments(parsedMap)
        )
      }
    }
  }
}