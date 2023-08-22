package me.blast.core

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

class CordexListener(private val cordex: Cordex): MessageCreateListener {
  override fun onMessageCreate(event: MessageCreateEvent) {
    if(!event.messageAuthor.isRegularUser) return
    
    val prefix = cordex.prefix(event.server.orElse(null)?.id ?: -1)
    if(event.messageContent.startsWith(prefix)) {
      cordex.executeCommand(event, prefix)
    }
  }
}