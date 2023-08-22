package me.blast.core

import me.blast.command.Context
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.slf4j.LoggerFactory

class Cordex(val prefix: (Long) -> String, private val handler: CordexCommands) {
  fun executeCommand(event: MessageCreateEvent, prefix: String) {
    val commandName = event.messageContent.substring(prefix.length).split(Regex("\\s+"), 1)[0].lowercase()

    handler.getCommands()[commandName]?.execute(
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
  
  companion object {
    const val VERSION = "0.1"
    
    val logger = LoggerFactory.getLogger(Cordex::class.java)
  }
}

class CordexBuilder(token: String) {
  var prefix: (Long) -> String = { "!" }
  val handler = CordexCommands()
  val api: DiscordApiBuilder = DiscordApiBuilder().setToken(token)
  
  fun prefix(lazy: (Long) -> String) { prefix = lazy }
  
  fun config(block: DiscordApiBuilder.() -> Unit): DiscordApiBuilder = api.apply(block)
  
  fun commands(block: CordexCommands.() -> Unit) = handler.apply(block)
}

inline fun cordex(
  token: String,
  block: (CordexBuilder.() -> Unit)
): List<DiscordApi> = CordexBuilder(token).run {
  block()
  api.loginAllShards().map {
    it.join().apply {
      addListener(CordexListener(Cordex(prefix, handler)))
    }
  }
}